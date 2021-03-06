/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.spark.dataset

import com.github.dnvriend.TestSpec
import com.github.dnvriend.spark._
import com.github.dnvriend.spark.datasources.person.Person
import org.apache.spark.sql.Dataset

class DatasetTest extends TestSpec {
  lazy val xs = Seq(
    Person(1, "foo", 30),
    Person(2, "bar", 21),
    Person(3, "baz", 25),
    Person(4, "jaz", 40),
    Person(5, "bab", 50)
  )

  it should "typed Dataset operations: count" in withSparkSession { spark =>
    import spark.implicits._
    val ds: Dataset[Person] = xs.toDS()
    ds.count() shouldBe 5
  }

  it should "untyped Dataset operations: (aka DataFrame, everything is a Row)" in withSparkSession { spark =>
    import spark.implicits._
    val ds = xs.toDS()
    ds.createOrReplaceTempView("people")
    ds.sqlContext.sql("SELECT COUNT(*) FROM people") // Array[Row]
      .head.getLong(0) shouldBe 5
  }

  it should "count SQL, convert back to typed with .as[Long]" in withSparkSession { spark =>
    import spark.implicits._
    val ds = xs.toDS()
    ds.createOrReplaceTempView("people")
    ds.sqlContext.sql("SELECT COUNT(*) FROM people").as[Long].head() shouldBe 5
  }

  it should "count using dataset operations" in withSparkSession { spark =>
    import spark.implicits._
    val ds = xs.toDS()
    ds.count() shouldBe 5
  }

  it should "filter a ds" in withSparkSession { spark =>
    import spark.implicits._
    val ds = xs.toDS()
    ds.filter(_.age < 30).count shouldBe 2
    ds.filter(_.age > 30).count shouldBe 2
    ds.filter(_.age >= 30).count shouldBe 3
  }

  it should "load people parquet" in withSparkSession { spark =>
    val people = spark.read.parquet(TestSpec.PeopleParquet)
    people.count shouldBe 5
  }

  it should "load purchase_items parquet" in withSparkSession { spark =>
    val people = spark.read.parquet(TestSpec.PurchaseItems)
    people.count shouldBe 25
  }

  it should "load transactions parquet" in withSparkSession { spark =>
    import spark.implicits._
    val tx = spark.read.parquet(TestSpec.Transactions).as[Transaction]
    tx.count shouldBe 1000
  }
}
