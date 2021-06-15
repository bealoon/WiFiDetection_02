package com.ai.wifidetection.utils.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.ai.wifidetection.R

class WarningDialog(context: Context?, themeResId: Int) : Dialog(context!!, themeResId) {

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var negativeButtonContent: String? = null
        private var negative2ButtonContent: String? = null
        private var positiveButtonContent: String? = null
        private var buttonClickListener: OnDialogClickListener? = null
        private var contentView: View? = null
        private var color: Int = 0
        private var withOffSize: Float = 0.toFloat()
        private var heightOffSize: Float = 0.toFloat()


        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setTitle(title: Int): Builder {
            this.title = context.getText(title) as String
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessageColor(color: Int): Builder {
            this.color = color
            return this
        }

        fun  setOnClickListener(listener: OnDialogClickListener): Builder{
            this.buttonClickListener = listener
            return this
        }

        fun setNegativeButton(text: String): Builder {
            this.negativeButtonContent = text
            return this
        }

        fun setNegativeButton(textId: Int): Builder {
            this.negativeButtonContent = context.getText(textId) as String
            return this
        }

        fun setPositiveButton(text: String): Builder {
            this.positiveButtonContent = text
            return this
        }

        fun setPositiveButton(textId: Int): Builder {
            this.positiveButtonContent = context.getText(textId) as String
            return this
        }

        fun setNegative2Button(text: String): Builder {
            this.negative2ButtonContent = text
            return this
        }

        fun setNegative2Button(textId: Int): Builder {
            this.negative2ButtonContent = context.getText(textId) as String
            return this
        }

        fun setContentView(v: View): Builder {
            this.contentView = v
            return this
        }

        fun setWith(v: Float): Builder {
            this.withOffSize = v
            return this
        }

        fun setHeightOffSize(v: Float): Builder {
            this.heightOffSize = v
            return this
        }

        fun create(): WarningDialog {
            /**
             * 利用自定义的样式初始化Dialog
             */
            val dialog = WarningDialog(context, R.style.CustomDialog)

            /**
             * 下面就初始化Dialog的布局页面
             */
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogLayoutView = inflater.inflate(R.layout.layout_dialog_alarm, null)
            dialog.addContentView(
                dialogLayoutView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            if (color != 0) {
                val viewById = dialogLayoutView.findViewById<View>(R.id.dialog_content) as TextView
                viewById.setTextColor(color)
            }

            if (null == message || message!!.isEmpty()){
                (dialogLayoutView.findViewById<View>(R.id.dialog_content) as TextView).text = ""
            }else{
                (dialogLayoutView.findViewById<View>(R.id.dialog_content) as TextView).text = message
            }

            if (null == title || title!!.isEmpty()){
                (dialogLayoutView.findViewById<View>(R.id.dialog_title) as TextView).visibility = View.GONE
            }else{
                (dialogLayoutView.findViewById<View>(R.id.dialog_title) as TextView).text = title
            }

            (dialogLayoutView.findViewById<View>(R.id.btn_positive) as TextView).text = positiveButtonContent
            (dialogLayoutView.findViewById<View>(R.id.btn_negative) as TextView).text = negativeButtonContent
            (dialogLayoutView.findViewById<View>(R.id.btn_negative2) as TextView).text = negative2ButtonContent

            if (buttonClickListener != null) {
                (dialogLayoutView.findViewById<View>(R.id.btn_positive) as Button)
                    .setOnClickListener { buttonClickListener?.onPositiveButtonClick(dialog) }
                (dialogLayoutView.findViewById<View>(R.id.btn_negative) as Button)
                    .setOnClickListener { buttonClickListener?.onNegativeButtonClick(dialog) }
                (dialogLayoutView.findViewById<View>(R.id.btn_negative2) as Button)
                    .setOnClickListener { buttonClickListener?.onNegative2ButtonClick(dialog) }
            }
            /**
             * 将初始化完整的布局添加到dialog中
             */
            dialog.setContentView(dialogLayoutView)
            /**
             * 禁止点击Dialog以外的区域时Dialog消失
             */
            dialog.setCanceledOnTouchOutside(false)


            val window = dialog.window
            val context = this.context as Activity
            val windowManager = context.windowManager

            val defaultDisplay = windowManager.defaultDisplay

            val attributes = window!!.attributes

            if (withOffSize.toDouble() != 0.0) {

                attributes.width = (defaultDisplay.width * withOffSize).toInt()
            } else {
                attributes.width = (defaultDisplay.width * 0.77).toInt()

            }
            if (heightOffSize.toDouble() != 0.0) {

                attributes.height = (defaultDisplay.height * heightOffSize).toInt()
            }
            window.attributes = attributes
            return dialog
        }
    }

    interface OnDialogClickListener{
        fun onPositiveButtonClick(dialogInterface: DialogInterface)
        fun onNegativeButtonClick(dialogInterface: DialogInterface)
        fun onNegative2ButtonClick(dialogInterface: DialogInterface)
    }

}