/*
 * Copyright (C) 2016 Litote
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
package org.litote.kmongo.async

import org.bson.types.Binary
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.MongoOperator.binary
import org.litote.kmongo.MongoOperator.type
import org.litote.kmongo.async.BinaryTest.BinaryFriend
import org.litote.kmongo.json
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BinaryTest : KMongoAsyncBaseTest<BinaryFriend>() {

    data class BinaryFriend(val _id: Binary, var name: String = "none")

    lateinit var friendId: Binary

    @Before
    fun setup() {
        friendId = Binary("kmongo".toByteArray())
    }

    @Test
    fun testUpdate() {
        val expectedId = Binary("friend2".toByteArray())
        val expected = BinaryFriend(expectedId, "friend2")

        col.insertOne(expected) { _, _ ->
            expected.name = "new friend"
            col.updateOne("{_id:${expectedId.json}}", expected) { _, _ ->
                col.findOne("{_id:${expectedId.json}}") { r, _ ->
                    asyncTest { assertEquals(expected, r) }
                }
            }
        }
    }

    @Test
    fun testInsert() {
        val expectedId = Binary("friend".toByteArray())
        val expected = BinaryFriend(expectedId, "friend")

        col.insertOne(expected, { _, _ ->
            col.findOne("{_id:${expectedId.json}}", { r, _ ->
                asyncTest { assertEquals(expected, r) }
            })
        })
    }


    @Test
    fun testRemove() {
        col.deleteOne("{_id:${friendId.json}}", { _, _ ->
            col.findOne("{_id:${friendId.json}}", { r, _ ->
                asyncTest { assertNull(r) }
            })
        })
    }

    @Test
    fun canMarhsallBinary() {

        val doc = BinaryFriend(Binary("abcde".toByteArray()))

        col.insertOne(doc, { _, _ ->
            col.count("{'_id' : { $binary : 'YWJjZGU=' , $type : '0'}}", { count, _ ->
                col.findOne("{_id:${doc._id.json}}", { r, _ ->
                    asyncTest {
                        assertEquals(1, count)
                        assertEquals(doc._id.type, r!!._id.type)
                        assertArrayEquals(doc._id.data, r._id.data)
                    }
                })
            })
        })
    }

}

