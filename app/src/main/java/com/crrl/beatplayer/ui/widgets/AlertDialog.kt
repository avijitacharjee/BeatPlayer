/*
 * Copyright (c) 2020. Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.ui.widgets

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemStyle
import com.crrl.beatplayer.ui.widgets.stylers.AlertType
import com.crrl.beatplayer.ui.widgets.stylers.ItemStyle
import com.crrl.beatplayer.ui.widgets.views.BottomSheetAlert
import com.crrl.beatplayer.ui.widgets.views.DialogAlert
import com.crrl.beatplayer.ui.widgets.views.InputDialog

class AlertDialog(
    private var title: String,
    private var message: String,
    private var style: ItemStyle,
    private var type: AlertType,
    private val inputText: String = ""
) {

    private var theme: AlertType? = AlertType.DIALOG
    private var actions: ArrayList<AlertItemAction> = ArrayList()
    private var alert: DialogFragment? = null

    /**
     * Add Item to AlertDialog
     * If you are using InputDialog, you can only add 2 actions
     * that will appear at the dialog bottom
     * @param item: AlertItemAction
     */
    fun addItem(item: AlertItemAction) {
        actions.add(item)
    }

    /**
     * Receives an Activity (AppCompatActivity), It's is necessary to getContext and show AlertDialog
     * @param activity: AppCompatActivity
     */
    fun show(activity: AppCompatActivity) {
        alert = when (type) {
            AlertType.BOTTOM_SHEET -> BottomSheetAlert(title, message, actions, style)
            AlertType.DIALOG -> DialogAlert(title, message, actions, style)
            AlertType.INPUT -> InputDialog(title, message, actions, style, inputText)
        }
        alert?.show(activity.supportFragmentManager, alert?.tag)
    }

    /**
     * Set type for alert. Choose between "AlertType.DIALOG" and "AlertType.BOTTOM_SHEET"
     * @param type: AlertType
     */
    fun setType(type: AlertType) {
        this.theme = type
    }

    /**
     * Update all style in the application
     * @param style: AlertType
     */
    fun setStyle(style: AlertItemStyle) {
        this.style = style
    }

    /**
     * Changes the style directly
     * @return style: AlertItemStyle
     */
    fun getStyle(): ItemStyle {
        return this.style
    }
}