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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.crrl.beatplayer.R
import com.crrl.beatplayer.interfaces.ItemListener
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.ui.widgets.stylers.InputStyle
import com.crrl.beatplayer.ui.widgets.stylers.ItemStyle
import com.crrl.beatplayer.utils.GeneralUtils.drawRoundRectShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.input_dialog_item.view.*
import kotlinx.android.synthetic.main.input_dialog_parent.view.*
import java.util.*

class InputDialog(
    private val title: String,
    private val message: String,
    private val bottomActions: ArrayList<AlertItemAction>,
    private val style: ItemStyle,
    private val inputText: String
) : BottomSheetDialogFragment(), ItemListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetAlertTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate base view
        val view = inflater.inflate(R.layout.input_dialog_parent, container, false)

        // Set up view
        initView(view)

        return view
    }

    private fun initView(view: View) {
        style as InputStyle
        view.title.apply {
            if (this@InputDialog.title.isEmpty()) {
                visibility = View.GONE
            } else {
                text = this@InputDialog.title
            }
            setTextColor(style.textColor)
        }


        view.sub_title.apply {
            if (message.isEmpty()) {
                visibility = View.GONE
            } else {
                text = message
            }
            setTextColor(style.textColor)
        }

        inflateActionsView(view.item_container)

        // Configuring View Parent
        val background = drawRoundRectShape(
            view.container.layoutParams.width,
            view.container.layoutParams.height,
            style.backgroundColor,
            style.cornerRadius
        )

        view.container.background = background
        view.sepMid.setBackgroundColor(style.textColor)

        view.cancel.apply {
            val item = bottomActions[0]
            text = item.title

            updateItem(this, item)

            setOnClickListener {

                item.input = view.text.text.toString()

                dismiss()

                //Add root view
                item.root = view

                //Execute listeners' methods for each case (Kotlin or Java)
                item.action?.invoke(item)
                item.actionListener?.onAlertItemClick(item)
            }
        }

        view.ok.apply {
            val item = bottomActions[1]
            text = item.title

            updateItem(this, item)

            setOnClickListener {

                item.input = view.text.text.toString()
                dismiss()

                //Execute listeners' methods for each case (Kotlin or Java)
                item.action?.invoke(item)
                item.actionListener?.onAlertItemClick(item)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun inflateActionsView(actionsLayout: LinearLayout) {
        style as InputStyle
        val view = LayoutInflater.from(context).inflate(
            R.layout.input_dialog_item,
            null
        ).apply {
            text.apply {
                hint = inputText
                setTextColor(style.textColor)
                setHintTextColor(style.hintTextColor)
                background =
                    drawRoundRectShape(layoutParams.width, layoutParams.height, style.inputColor)
                requestFocus()
                setText(style.text)
                selectAll()
            }
        }
        actionsLayout.addView(view)
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    /**
     * This method sets the views style
     * @param view: View
     * @param alertItemAction: AlertItemAction
     */
    override fun updateItem(view: View, alertItemAction: AlertItemAction) {
        style as InputStyle
        val action = view as Button

        // Action text color according to AlertActionStyle
        if (context != null) {
            when (alertItemAction.theme) {
                AlertItemTheme.DEFAULT -> {
                    action.setTextColor(style.hintTextColor)
                }
                AlertItemTheme.CANCEL -> {
                    action.setTextColor(ContextCompat.getColor(context!!, R.color.error))
                }
                AlertItemTheme.ACCEPT -> {
                    action.setTextColor(style.acceptColor)
                }
            }
        }
    }
}
