package com.dotography.balancetracker.model

import java.math.MathContext
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedDate
import net.liftweb.mapper.MappedDateTime
import net.liftweb.mapper.MappedDecimal
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedLongForeignKey


trait Balance[A <: Balance[A]] extends LongKeyedMapper[A] with IdPK {  
  self: A =>
  implicit val formats = net.liftweb.json.DefaultFormats
//  class blah extends MappedLongForeignKey(this, IncomeCategory)
//  def mappedCategory[O <: MappedLongForeignKey[O]]: MappedLongForeignKey[A, O] = new blah// with categoryField[A]
  
//  trait categoryField[A <: Balance[A]] extends MappedLong[A] {
//    override def dbColumnName = "category_id"
//  }

  object amount extends MappedDecimal(this, MathContext.DECIMAL64, 2)
  object effectiveDate extends MappedDate(this) {
    override def dbColumnName = "effective_date"
  }
  object created extends MappedDateTime(this)
  object lastModified extends MappedDateTime(this) {
    override def dbColumnName = "last_modified"
  }
  def toJson = {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    JObject(List(id.asJsonField.orNull, mappedCategory.asJsonField.orNull))
  }
}

//object Income extends Income with LongKeyedMetaMapper[Income]
//
//class Income extends Balance[Income] {
//  override def getSingleton = Income
//  object category extends blah// with categoryField[Income]
//  override def mappedCategory = new blah
//}


//object Expense extends Expense with LongKeyedMetaMapper[Expense]
//
//class Expense extends Balance[Expense] {
//  override def getSingleton = Expense
//  object category extends MappedLongForeignKey(this, ExpenseCategory) with categoryField[Expense]
//  override def mappedCategory = category
//}

