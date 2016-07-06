package com.dotography.balancetracker.model

import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedString

trait Configuration[A <: Configuration[A]] extends LongKeyedMapper[A] with IdPK {  
  self: A =>
  object description extends MappedString[A](this.asInstanceOf[A], 50)
  implicit val formats = net.liftweb.json.DefaultFormats
  
  def toJson = {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    JObject(List(id.asJsonField.orNull, description.asJsonField.orNull))
  }
}

class IncomeCategory extends Configuration[IncomeCategory] {
  def getSingleton = IncomeCategory
}

object IncomeCategory extends IncomeCategory with LongKeyedMetaMapper[IncomeCategory] {
  override def dbTableName = "income_category"
}

class ExpenseCategory extends Configuration[ExpenseCategory] {
  def getSingleton = ExpenseCategory
}


object ExpenseCategory extends ExpenseCategory with LongKeyedMetaMapper[ExpenseCategory] {
  override def dbTableName = "expense_category"
}

