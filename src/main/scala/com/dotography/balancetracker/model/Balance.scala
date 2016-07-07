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
  //def mappedCategory[O <: LongKeyedMapper[O]]: MappedLongForeignKey[A, O]// with categoryField[A]
  abstract class mappedCategory[A <: Balance[A], O <: LongKeyedMapper[O]](a : A, b : LongKeyedMetaMapper[O]) extends MappedLongForeignKey[A, O](a, b) {
    type B <: Balance[A]
    type P <: LongKeyedMapper[O]
    def getMe() : mappedCategory[B,P]
    override def dbColumnName = "category_id"
  }

  def categoryM [A <: Balance[A], O <: LongKeyedMapper[O]] : mappedCategory[A,O]
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
    //JObject(List(id.asJsonField.orNull, categoryM.asJsonField.orNull))
  }
}

object Income extends Income with LongKeyedMetaMapper[Income]

class Income extends Balance[Income] {
  override def getSingleton = Income
  //object category extends MappedLongForeignKey(this, IncomeCategory)// with categoryField[Income]
  //override def categoryM [Income] : mappedCategory[Income] = new mappedCategory[IncomeCategory](IncomeCategory)//= new category[IncomeCategory](IncomeCategory)
  //override def mappedCategory = category.asInstanceOf[MappedLongForeignKey[Income, IncomeCategory]]
  class category(income : Income, incomeCategory : IncomeCategory) extends mappedCategory[Income, IncomeCategory](income, incomeCategory.asInstanceOf[LongKeyedMetaMapper[IncomeCategory]]) {
    type B = Income
    type P = IncomeCategory
    def getMe() : mappedCategory[Income, IncomeCategory] = this
  }
  object me extends category(this, IncomeCategory)
  override def categoryM [A <: Balance[A], O <: LongKeyedMapper[O]]: mappedCategory[Income,IncomeCategory]= me.getMe
}

//object Expense extends Expense with LongKeyedMetaMapper[Expense]
//
//class Expense extends Balance[Expense] {
//  override def getSingleton = Expense
//  object category extends MappedLongForeignKey(this, ExpenseCategory) with categoryField[Expense]
//  override def mappedCategory = category
//}

