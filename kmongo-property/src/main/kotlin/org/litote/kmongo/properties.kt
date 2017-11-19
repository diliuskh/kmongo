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

import org.bson.codecs.pojo.annotations.BsonId
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 *
 */
operator fun <T1, T2> KProperty<T1?>.div(p2: KProperty1<T1, T2?>): KPropertyPath<T2?> = KPropertyPath(this, p2)

operator fun <T1, T2> KProperty<T1?>.div(p2: KProperty<T2?>): KPropertyPath<T2?> = KPropertyPath(this, p2)


internal fun <T> KProperty<T>.path(): String {
    //TODO specific mapping
    return /*annotations
            .find { it.annotationClass == JsonProperty::class }
            ?.let { (it as JsonProperty).value }
            ?:*/ annotations
            .find { it.annotationClass == BsonId::class }
            ?.let { "_id" }
            ?: this.name
}