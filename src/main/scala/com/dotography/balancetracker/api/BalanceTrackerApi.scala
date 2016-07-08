package com.dotography.balancetracker.api

import net.liftweb.http.rest.RestHelper
import com.dotography.balancetracker.model.ExpenseCategory
import com.dotography.balancetracker.model.ExpenseTemp
import com.dotography.balancetracker.model.IncomeCategory
import com.dotography.balancetracker.model.IncomeTemp
import net.liftweb.common.Loggable
import net.liftweb.json.JsonAST.JValue
import net.liftweb.mapper.By
import net.liftweb.util.Helpers.AsInt
import net.liftweb.http.JsonResponse
import net.liftweb.http.OkResponse
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonAST.JInt

object BalanceTrackerApi extends RestHelper with Loggable {  
  serve {
    case "status" :: Nil JsonGet req => status()
    case "incomecategories" :: Nil JsonGet req => getIncomeCategories()
    case "expensecategories" :: Nil JsonGet req => getExpenseCategories()
    case "income" :: Nil JsonPut ((income, req)) => addIncome(income)
    case "expense" :: Nil JsonPut ((expense, req)) => addExpense(expense)
    case "income" :: AsInt(year) :: AsInt(month) :: AsInt(date) :: Nil JsonGet req => getIncome(year, month, date) 
    case "income" :: AsInt(year) :: AsInt(month) :: Nil JsonGet req => getIncome(year, month) 
    case "expense" :: AsInt(year) :: AsInt(month) :: AsInt(date) :: Nil JsonGet req => getExpense(year, month, date) 
    case "expense" :: AsInt(year) :: AsInt(month) :: Nil JsonGet req => getExpense(year, month) 
  }
  
  def status() = {
    new OkResponse
  }
  
  def getIncomeCategories() : JArray = {
    JArray(IncomeCategory.findAll.map(_.toJson))
  }
  
  def getExpenseCategories() : JArray = {
    JArray(ExpenseCategory.findAll.map(_.toJson))
  }
  
  def addIncome(income : JValue) : JsonResponse = {
    val newId : Long = IncomeTemp.create.amount(BigDecimal( (income \ "amount").extract[String] ))
        .category((income \ "category_id").extract[Int])
        .detail(Option((income \ "detail").extract[String]).getOrElse(""))
        .year((income \ "year").extract[Int])
        .month((income \ "month").extract[Int])
        .date((income \ "date").extract[Int])
        .created(System.currentTimeMillis)
        .lastModified(System.currentTimeMillis)
        .saveMe
        .id.get
    JsonResponse(JInt(newId), List(("Content-Type", "application/json; charset=utf-8")), List(), 201)
  }
  
  def addExpense(expense : JValue) = {
    val newId : Long = ExpenseTemp.create.amount(BigDecimal( (expense \ "amount").extract[String] ))
        .category((expense \ "category_id").extract[Int])
        .detail(Option((expense \ "detail").extract[String]).getOrElse(""))
        .year((expense \ "year").extract[Int])
        .month((expense \ "month").extract[Int])
        .date((expense \ "date").extract[Int])
        .created(System.currentTimeMillis)
        .lastModified(System.currentTimeMillis)
        .saveMe
        .id.get
    JsonResponse(JInt(newId), List(("Content-Type", "application/json; charset=utf-8")), List(), 201)
  }
  
  def getIncome(year : Int, month : Int, date : Int) : JArray = {
    JArray(IncomeTemp.findAll(By(IncomeTemp.year, year), By(IncomeTemp.month, month), By(IncomeTemp.date, date)).map(_.toJson))
  }
  
  def getIncome(year : Int, month : Int) : JArray  = {
    JArray(IncomeTemp.findAll(By(IncomeTemp.year, year), By(IncomeTemp.month, month)).map(_.toJson))
  }
  
  def getExpense(year : Int, month : Int, date : Int) : JArray  = {
    JArray(ExpenseTemp.findAll(By(ExpenseTemp.year, year), By(ExpenseTemp.month, month), By(ExpenseTemp.date, date)).map(_.toJson))
  }
  
  def getExpense(year : Int, month : Int) : JArray  = {
    JArray(ExpenseTemp.findAll(By(ExpenseTemp.year, year), By(ExpenseTemp.month, month)).map(_.toJson))
  }
}
