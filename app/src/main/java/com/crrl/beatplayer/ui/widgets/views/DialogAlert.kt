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

package com.crrl.beatplayer.ui.widgets.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.crrl.beatplayer.R
import com.crrl.beatplayer.interfaces.ItemListener
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemStyle
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.ui.widgets.stylers.ItemStyle
import com.crrl.beatplayer.utils.GeneralUtils.drawRoundRectShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogAlert(
    private val title: String,
    private val message: String,
    private val itemList: ArrayList<AlertItemAction>,
    private val style: ItemStyle
) : DialogFragment(), ItemListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetAlertTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate base view
        val view = inflater.inflate(R.layout.parent_dialog_layout, container, false)

        // Set up view
        initView(view)

        return view
    }

    private fun initView(view: View?) {
        style as AlertItemStyle
        //Finding views
        val titleView = view!!.findViewById<TextView>(R.id.title)
        val subTitleView = view.findViewById<TextView>(R.id.sub_title)
        val cancel = view.findViewById<Button>(R.id.cancel)
        val container = view.findViewById<LinearLayout>(R.id.container)

        titleView.apply {
            if (title.isEmpty()) {
                visibility = View.GONE
            } else {
                text = title
            }
            setTextColor(style.textColor)
        }

        subTitleView.apply {
            if (message.isEmpty()) {
                visibility = View.GONE
            } else {
                text = message
            }
            setTextColor(style.textColor)
        }

        // Configuring View Parent
        val background = drawRoundRectShape(
            container.layoutParams.width,
            container.layoutParams.height,
            style.backgroundColor
        )

        cancel.visibility = View.GONE
        container.background = background

        // Inflate action views
        inflateActionsView(view.findViewById(R.id.item_container), itemList)
    }

    private fun inflateActionsView(actionsLayout: LinearLayout, items: ArrayList<AlertItemAction>) {
        style as AlertItemStyle
        items.map { item ->

            // Finding Views
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_item, null)
            val action = view.findViewById<Button>(R.id.action)
            val indicator = view.findViewById<View>(R.id.indicator)

            action.text = item.title

            // Click listener for action.
            action.setOnClickListener {
                dismiss()

                //Store selectedState
                val oldState = item.selected

                //Add root view
                item.root = view

                //Execute listeners' methods for each case (Kotlin on Java)
                item.action?.invoke(item)
                item.actionListener?.onAlertItemClick(item)

                // Check if selected state changed
                if (oldState != item.selected) {
                    //Clean Selection State
                    cleanSelection(items, item)
                    //Update Item Style
                    updateItem(view, item)
                }
            }
            //Set style first time
            updateItem(view, item)

            //Set separator view background
            indicator.setBackgroundColor(style.textColor)

            // Add child to its parent
            actionsLayout.addView(view)
        }
    }

    /**
     * This method clears the selection states for each item in the array.
     * @param items: java.util.ArrayList<AlertItemAction> All the items that will be modified
     * @param currentItem: AlertItemAction to save current item state
     */
    private fun cleanSelection(
        items: java.util.ArrayList<AlertItemAction>,
        currentItem: AlertItemAction
    ) {
        for (item in items) {
            if (item != currentItem) item.selected = false
        }
    }

    /**
     * This method sets the views style
     * @param view: View
     * @param alertItemAction: AlertItemAction
     */
    override fun updateItem(view: View, alertItemAction: AlertItemAction) {
        style as AlertItemStyle
        val action = view.findViewById<Button>(R.id.action)

        // Action text color according to AlertActionStyle
        if (context != null) {
            when (alertItemAction.theme) {
                AlertItemTheme.DEFAULT -> {
                    if (alertItemAction.selected) {
                        action.setTextColor(style.selectedTextColor)
                    } else {
                        action.setTextColor(style.textColor)
                    }
                }
                AlertItemTheme.CANCEL -> {
                    action.setTextColor(ContextCompat.getColor(context!!, R.color.error))
                }
                AlertItemTheme.ACCEPT -> {
                    action.setTextColor(ContextCompat.getColor(context!!, R.color.success))
                }
            }
        }
    }
}