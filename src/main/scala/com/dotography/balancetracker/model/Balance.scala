package com.dotography.balancetracker.model

import java.math.MathContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import net.liftweb.common.Box
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JString
import net.liftweb.mapper.By
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedDate
import net.liftweb.mapper.MappedDateTime
import net.liftweb.mapper.MappedDecimal
import net.liftweb.mapper.MappedInt
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedLongForeignKey
import net.liftweb.mapper.MappedString


trait Balance[A <: Balance[A]] extends LongKeyedMapper[A] with IdPK {  
  self: A =>
  implicit val formats = net.liftweb.json.DefaultFormats
  //def mappedCategory[O <: LongKeyedMapper[O]]: MappedLongForeignKey[A, O]// with categoryField[A]
  abstract class mappedCategory[A <: Balance[A], O <: LongKeyedMapper[O]](a : A, b : LongKeyedMetaMapper[O]) extends MappedLongForeignKey[A, O](a, b) {
    type B <: Balance[A]
    type P <: LongKeyedMapper[O]
    // def getMe() : mappedCategory[B,P]
    override def dbColumnName = "category_id"
  }

  //def categoryM [A <: Balance[A], O <: LongKeyedMapper[O]] : mappedCategory[A,O]
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

//object Income extends Income with LongKeyedMetaMapper[Income]
//
//class Income extends Balance[Income] {
//  override def getSingleton = Income
//  //object category extends MappedLongForeignKey(this, IncomeCategory)// with categoryField[Income]
//  //override def categoryM [Income] : mappedCategory[Income] = new mappedCategory[IncomeCategory](IncomeCategory)//= new category[IncomeCategory](IncomeCategory)
//  //override def mappedCategory = category.asInstanceOf[MappedLongForeignKey[Income, IncomeCategory]]
//  class category(income : Income, incomeCategory : IncomeCategory) extends mappedCategory[Income, IncomeCategory](income, incomeCategory.asInstanceOf[LongKeyedMetaMapper[IncomeCategory]]) {
//    type B = Income
//    type P = IncomeCategory
//    def getMe() : mappedCategory[Income, IncomeCategory] = this
//  }
//  object me extends category(this, IncomeCategory)
//  override def categoryM [A <: Balance[A], O <: LongKeyedMapper[O]]: mappedCategory[Income,IncomeCategory]= me.getMe
//}

//object Expense extends Expense with LongKeyedMetaMapper[Expense]
//
//class Expense extends Balance[Expense] {
//  override def getSingleton = Expense
//  object category extends MappedLongForeignKey(this, ExpenseCategory) with categoryField[Expense]
//  override def mappedCategory = category
//}

// ===================== Temporary Solution here ===============================
trait BalanceTemp[A <: BalanceTemp[A]] extends LongKeyedMapper[A] with IdPK {  
  self: A =>
  implicit val formats = net.liftweb.json.DefaultFormats

  object amount extends MappedDecimal(this, MathContext.DECIMAL64, 2)
  object detail extends MappedString(this, 255)
  object year extends MappedInt(this)
  object month extends MappedInt(this)
  object date extends MappedInt(this) {
    override def dbColumnName = "date"
  }
  object created extends MappedLong(this)
  object lastModified extends MappedLong(this) {
    override def dbColumnName = "last_modified"
  }
  def categoryJsonField : Box[JField]
  def categoryDesc : String
  def toJson = {
    import net.liftweb.json.JsonDSL._
    val flowDateFormatter : DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val timestampFormatter : DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val flowDate = LocalDate.of(year.get, Month.of(month.get), date.get)
    val createdTime : Instant = Instant.ofEpochMilli(created.get)
    val lastModifiedTime : Instant = Instant.ofEpochMilli(lastModified.get)
    
    JObject(List(id.asJsonField.orNull, categoryJsonField.orNull, amount.asJsonField.orNull,
      year.asJsonField.orNull, month.asJsonField.orNull, date.asJsonField.orNull, 
      created.asJsonField.orNull, lastModified.asJsonField.orNull,
      JField("flowDate", JString(flowDate.format(flowDateFormatter))),
      JField("createTime", JString(LocalDateTime.ofInstant(createdTime, ZoneId.systemDefault).format(timestampFormatter))),
      JField("lastModifiedTime", JString(LocalDateTime.ofInstant(lastModifiedTime, ZoneId.systemDefault).format(timestampFormatter))),
      JField("categoryDesc", JString(categoryDesc))))
  }
}

object IncomeTemp extends IncomeTemp with LongKeyedMetaMapper[IncomeTemp] {  
  override def dbTableName = "income"
}

class IncomeTemp extends BalanceTemp[IncomeTemp] {
  override def getSingleton = IncomeTemp
  object category extends MappedLongForeignKey(this, IncomeCategory) {
    override def dbColumnName = "category_id"
  }
  
  override def categoryJsonField : Box[JField] = {
    category.asJsonField
  }
  
  override def categoryDesc : String = {
    IncomeCategory.findAll(By(IncomeCategory.id, category)).headOption.getOrElse(IncomeCategory.create.description("Not found")).description
  }
}

object ExpenseTemp extends ExpenseTemp with LongKeyedMetaMapper[ExpenseTemp] {  
  override def dbTableName = "expense"
}

class ExpenseTemp extends BalanceTemp[ExpenseTemp] {
  override def getSingleton = ExpenseTemp
  object category extends MappedLongForeignKey(this, ExpenseCategory) {
    override def dbColumnName = "category_id"
  }
  
  override def categoryJsonField : Box[JField] = {
    category.asJsonField
  }
  
  override def categoryDesc : String = {
    ExpenseCategory.findAll(By(ExpenseCategory.id, category)).headOption.getOrElse(ExpenseCategory.create.description("Not found")).description
  }
}