package com.dotography.balancetracker.api

import net.liftweb.http.rest.RestHelper
import com.dotography.balancetracker.model.ExpenseCategory
import com.dotography.balancetracker.model.IncomeCategory
import net.liftweb.common.Loggable
import net.liftweb.http.OkResponse
import net.liftweb.json.JsonAST.JArray

object BalanceTrackerApi extends RestHelper with Loggable {
  
  serve {
    case "status" :: Nil JsonGet req => status()
    case "incomecategories" :: Nil JsonGet req => getIncomeCategories()
    case "expensecategories" :: Nil JsonGet req => getExpenseCategories()
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
}
