package com.saneforce.fieldcopilot

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Public entry point of the Field Copilot module.
 *
 * Host apps either place their own FAB in XML and call [show] from its click
 * listener, or call [addFabTo] to inject a ready-made chat FAB into any
 * container with zero layout changes.
 */
object FieldCopilot {

    /** Opens Field Copilot in its own Activity. */
    fun show(activity: Activity, config: FieldCopilotConfig) {
        activity.startActivity(
            Intent(activity, FieldCopilotDialog::class.java)
                .putExtra(FieldCopilotDialog.EXTRA_URL, config.buildUrl())
        )
    }

    /**
     * Adds a bottom-end chat FAB to [container] and wires it to [show].
     * [configProvider] is invoked on every tap so the latest session values
     * are always sent to the chatbot page.
     */
    fun addFabTo(
        container: ViewGroup,
        activity: Activity,
        configProvider: () -> FieldCopilotConfig,
    ): FloatingActionButton {
        val context = container.context
        val margin = context.resources
            .getDimensionPixelSize(R.dimen.fieldcopilot_fab_margin)
        val fab = FloatingActionButton(context).apply {
            setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.fieldcopilot_ic_bot)
            )
            imageTintList = ContextCompat.getColorStateList(context, android.R.color.white)
            backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.fieldcopilot_accent)
            contentDescription = context.getString(R.string.fieldcopilot_title)
        }
        val params = when (container) {
            is RelativeLayout -> RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                addRule(RelativeLayout.ALIGN_PARENT_END)
                setMargins(margin, margin, margin, margin)
            }

            else -> FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply { setMargins(margin, margin, margin, margin) }
        }
        fab.setOnClickListener { show(activity, configProvider()) }
        container.addView(fab, params)
        return fab
    }
}
