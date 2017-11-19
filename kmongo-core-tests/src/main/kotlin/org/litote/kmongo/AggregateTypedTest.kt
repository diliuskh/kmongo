/*
 * Copyright (C) 2017 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.kmongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.AggregateTypedTest.Article
import org.litote.kmongo.model.Friend
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class AggregateTypedTest : AllCategoriesKMongoBaseTest<Article>() {

    data class Article(val title: String, val author: String, val tags: List<String>) {

        constructor(title: String, author: String, vararg tags: String) : this(title, author, tags.asList())
    }

    lateinit var friendCol: MongoCollection<Friend>

    @Before
    fun setup() {
        col.insertOne(Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"))
        col.insertOne(Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"))
        col.insertOne(Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"))

        friendCol = getCollection<Friend>()
        friendCol.insertOne(Friend("William"))
        friendCol.insertOne(Friend("John"))
        friendCol.insertOne(Friend("Richard"))
    }

    @After
    fun tearDown() {
        dropCollection<Friend>()
    }

    override fun getDefaultCollectionClass(): KClass<Article> = Article::class

    @Test
    fun canAggregate() {
        val l = col.aggregate<Article>(match(Article::author eq "Maberry Jonathan")).toList()
        assertEquals(1, l.size)
    }


    @Test
    fun canAggregateWithMultipleDocuments() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus")).toList()
        assertEquals(2, l.size)
        assertTrue(l.all { it.tags.contains("virus") })
    }

    @Test
    fun canAggregateParameters() {
        val tag = "pandemic"
        val l = col.aggregate<Article>(match(Article::tags contains tag)).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyMatch() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus", Article::tags contains "pandemic")).toList()
        assertEquals(1, l.size)
        assertEquals("World War Z", l.first().title)
    }

    @Test
    fun canAggregateWithManyOperators() {
        val l = col.aggregate<Article>(match(Article::tags contains "virus"), Aggregates.limit(1)).toList()
        assertEquals(1, l.size)
    }

    @Test
    fun shouldPopulateIds() {
        val l = friendCol.aggregate<Friend>(
                project(
                        mapOf(
                                Friend::_id to "\$_id",
                                Friend::name to "\$name"
                        ) as Map<KProperty<*>, String>
                )).toList()
        assertEquals(3, l.size)
        assertTrue(l.all { it._id != null })
        assertTrue(l.all { it.name != null })
        assertTrue(l.all { it.address == null })

        val l2 = friendCol.aggregate<Friend>(project(Friend::_id, Friend::name)).toList()
        assertEquals(3, l2.size)
        assertTrue(l2.all { it._id != null })
        assertTrue(l2.all { it.name != null })
        assertTrue(l2.all { it.address == null })

        val l3 = friendCol.aggregate<Friend>(project(Friend::name)).toList()
        assertEquals(3, l3.size)
        assertTrue(l3.all { it._id != null })
        assertTrue(l3.all { it.name != null })
        assertTrue(l3.all { it.address == null })

        val l4 = friendCol.aggregate<Friend>(project(Friend::_id to false, Friend::name to true)).toList()
        assertEquals(3, l4.size)
        assertTrue(l4.all { it._id == null })
        assertTrue(l4.all { it.name != null })
        assertTrue(l4.all { it.address == null })
    }
}