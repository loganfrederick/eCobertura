/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2009, 2010 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ecobertura.ui.util

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.ui.JavaUI

object JavaEditorOpener {
	def openAndReveal(javaElement: IJavaElement) = {
		val editorPart = JavaUI.openInEditor(javaElement)
		JavaUI.revealInEditor(editorPart, javaElement)
	}
}
