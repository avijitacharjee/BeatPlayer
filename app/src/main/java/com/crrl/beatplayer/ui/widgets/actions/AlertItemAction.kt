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

package com.crrl.beatplayer.ui.widgets.actions

import android.view.View
import com.crrl.beatplayer.interfaces.AlertItemActionListener
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme

class AlertItemAction {
    var title: String
    var action: ((AlertItemAction) -> Unit)?
    var actionListener: AlertItemActionListener?
    var theme: AlertItemTheme? = AlertItemTheme.DEFAULT
    var selected: Boolean
    var input: String? = null
    var root: View? = null

    constructor(
        title: String,
        selected: Boolean,
        theme: AlertItemTheme? = AlertItemTheme.DEFAULT,
        action: (AlertItemAction) -> Unit
    ) {
        this.title = title
        this.selected = selected
        this.action = action
        this.actionListener = null
        this.theme = theme
    }

    constructor(
        title: String,
        selected: Boolean,
        theme: AlertItemTheme,
        actionListener: AlertItemActionListener
    ) {
        this.title = title
        this.selected = selected
        this.actionListener = actionListener
        this.action = null
        this.theme = theme
    }
}