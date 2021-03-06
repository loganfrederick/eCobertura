/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2010 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ecobertura.core.data.filters

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import java.util.{ArrayList, List => JavaList}

import org.eclipse.debug.core._

/**
 * Helps creating ClassFilters from launch configurations 
 * or simply lists of class filters.
 */
object ClassFilters {
  def apply(launchConfiguration: ILaunchConfiguration) : ClassFilters = {
    val classFilterList = launchConfiguration.getAttribute("classFilters", 
        new ArrayList[String])
        
    ClassFilters((
        for (filterString <- classFilterList.asInstanceOf[JavaList[String]])
        yield ClassFilter(filterString)) : _*)
  }

  def apply(classFilterArray: ClassFilter*) : ClassFilters = {
    val classFilters = new ClassFilters
    for (classFilter <- classFilterArray) classFilters.add(classFilter)
    classFilters
  }
}

/**
 * Stores include/exclude class filters. Used as data model for the
 * launch configuration's "filters" tab.
 */
class ClassFilters {
  private var classFilters = ListBuffer[ClassFilter]()
  
  def toArray = classFilters.toArray
  
  def add(classFilter: ClassFilter) = classFilters += classFilter
  def remove(classFilter: ClassFilter) = classFilters -= classFilter
  
  def addToLaunchConfiguration(launchConfiguration: ILaunchConfigurationWorkingCopy) = {

    val javaList: JavaList[String] = new ArrayList[String]
    for (filter <- classFilters) javaList.add(filter.toAttributeString)
    launchConfiguration.setAttribute("classFilters", javaList)
  }
  
  override def toString = "ClassFilters(%s)".format(classFilters.toString)
  
  def isClassIncluded(reversedRelativePath: List[String]) : Boolean = 
    isClassIncluded(qualifiedClassNameFromReversedPath(reversedRelativePath), classFilters.toList)
  
  private def qualifiedClassNameFromReversedPath(reversedRelativePath: List[String]) = {
    reversedRelativePath.reverse.mkString(".")
  }

  private def isClassIncluded(className: String, classFilters: List[ClassFilter]) : Boolean = {
    classFilters match {
      case Nil => true
      case classFilter :: tail =>
          if (classFilter.includes(className)) isClassIncluded(className, tail) else false 
    }
  }
}