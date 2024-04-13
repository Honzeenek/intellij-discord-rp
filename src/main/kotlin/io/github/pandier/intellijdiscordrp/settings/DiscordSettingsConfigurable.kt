package io.github.pandier.intellijdiscordrp.settings

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import io.github.pandier.intellijdiscordrp.activity.ActivityDisplayMode
import io.github.pandier.intellijdiscordrp.service.DiscordService
import io.github.pandier.intellijdiscordrp.settings.ui.DslConfigurable
import io.github.pandier.intellijdiscordrp.settings.ui.tabbed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

private fun displayModeSettings(
    imageSettings: List<ImageSetting>,
    details: KMutableProperty0<String>,
    state: KMutableProperty0<String>,
    largeImage: KMutableProperty0<ImageSetting>,
    largeImageEnabled: KMutableProperty0<Boolean>,
    largeImageText: KMutableProperty0<String>,
    smallImage: KMutableProperty0<ImageSetting>,
    smallImageEnabled: KMutableProperty0<Boolean>,
    smallImageText: KMutableProperty0<String>,
): Panel.() -> Unit = {
    row("Details:") {
        textField()
            .align(AlignX.FILL)
            .bindText(details)
    }
    row("State:") {
        textField()
            .align(AlignX.FILL)
            .bindText(state)
    }

    // Large image settings
    lateinit var largeImageCheckBox: Cell<JBCheckBox>
    row {
        largeImageCheckBox = checkBox("Large image")
            .bindSelected(largeImageEnabled)
    }
    indent {
        row {
            label("Image:")
            comboBox(imageSettings)
                .bindItem(largeImage.toNullableProperty())
            label("Text:")
            textField()
                .bindText(largeImageText)
        }
    }.enabledIf(largeImageCheckBox.selected)

    // Small image settings
    lateinit var smallImageCheckBox: Cell<JBCheckBox>
    row {
        smallImageCheckBox = checkBox("Small image")
            .bindSelected(smallImageEnabled)
    }
    indent {
        row {
            label("Image:")
            comboBox(imageSettings)
                .bindItem(smallImage.toNullableProperty())
            label("Text:")
            textField()
                .bindText(smallImageText)
        }
    }.enabledIf(smallImageCheckBox.selected)
}

class DiscordSettingsConfigurable : DslConfigurable("Discord Rich Presence") {

    override fun createPanel(): DialogPanel = panel {
        val state = discordSettingsComponent.state

        row {
            checkBox("Try reconnecting on activity update")
                .bindSelected(state::reconnectOnUpdate)
        }

        row {
            val customApplicationIdCheckBox = checkBox("Custom application id:")
                .bindSelected(state::customApplicationIdEnabled)
            textField()
                .bindText(state::customApplicationId)
                .enabledIf(customApplicationIdCheckBox.selected)
        }

        row {
            label("Default display mode:")
            comboBox(ActivityDisplayMode.values().toList())
                .bindItem(state::defaultDisplayMode.toNullableProperty())
        }

        group("Display") {
            tabbed {
                tab(
                    "Application",
                    displayModeSettings(
                        imageSettings = listOf(ImageSetting.APPLICATION),
                        details = state::applicationDetails,
                        state = state::applicationState,
                        largeImage = state::applicationLargeImage,
                        largeImageEnabled = state::applicationLargeImageEnabled,
                        largeImageText = state::applicationLargeImageText,
                        smallImage = state::applicationSmallImage,
                        smallImageEnabled = state::applicationSmallImageEnabled,
                        smallImageText = state::applicationSmallImageText,
                    )
                )

                tab(
                    "Project",
                    displayModeSettings(
                        imageSettings = listOf(ImageSetting.APPLICATION),
                        details = state::projectDetails,
                        state = state::projectState,
                        largeImage = state::projectLargeImage,
                        largeImageEnabled = state::projectLargeImageEnabled,
                        largeImageText = state::projectLargeImageText,
                        smallImage = state::projectSmallImage,
                        smallImageEnabled = state::projectSmallImageEnabled,
                        smallImageText = state::projectSmallImageText,
                    )
                )

                tab(
                    "File",
                    displayModeSettings(
                        imageSettings = listOf(ImageSetting.APPLICATION, ImageSetting.FILE),
                        details = state::fileDetails,
                        state = state::fileState,
                        largeImage = state::fileLargeImage,
                        largeImageEnabled = state::fileLargeImageEnabled,
                        largeImageText = state::fileLargeImageText,
                        smallImage = state::fileSmallImage,
                        smallImageEnabled = state::fileSmallImageEnabled,
                        smallImageText = state::fileSmallImageText,
                    )
                )
            }
        }
    }

    override fun apply() {
        if (validateAndApply()) {
            val discordService = DiscordService.getInstance()
            discordService.scope.launch(Dispatchers.IO) {
                discordService.reconnect()
            }
        }
    }
}