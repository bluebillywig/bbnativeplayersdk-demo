package com.bluebillywig.bbnativeplayersdk_demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bluebillywig.bbnativeplayersdk.BBNativePlayer
import com.bluebillywig.bbnativeplayersdk.BBNativePlayerView
import com.bluebillywig.bbnativeplayersdk.BBNativePlayerViewDelegate
import com.bluebillywig.bbnativeshared.Logger
import com.bluebillywig.bbnativeshared.model.MediaClip
import com.bluebillywig.bbnativeshared.enums.ApiMethod

/**
 * A [DialogFragment] fullscreen subclass.
 * Use the [PlayerDialogFragment.show] method to show a fullscreen player in an overlay.
 */
class PlayerDialogFragment(private var jsonUrl: String, private var autoPlay: Boolean = true) : DialogFragment(), BBNativePlayerViewDelegate {
	private lateinit var player: BBNativePlayerView
	private lateinit var playerContainer: LinearLayout
	private lateinit var dialogView: View

	private var seekOffset = 0.0;

	// This constructor is used when rotating the android phone
	constructor() : this("")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)

		Logger.d("PlayerDialogFragment", "Saved instance state: $savedInstanceState")

		if (savedInstanceState != null) {
			jsonUrl = savedInstanceState.getString("jsonUrl") ?: ""
		}
		Logger.d("PlayerDialogFragment", "json url: $jsonUrl")

		player = BBNativePlayer.createPlayerView(requireActivity(), jsonUrl, mapOf("modalPlayer" to true))
		player.delegate = this
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		super.onCreateView(inflater, container, savedInstanceState)

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		dialogView = inflater.inflate(R.layout.dialog_player, null)

		// If we can get the playout background color, we can override it here
		dialogView.setBackgroundColor(Color.BLACK)

		playerContainer = dialogView.findViewById(R.id.playerContainerView)

		playerContainer.addView(player)

		if (autoPlay) {
			player.callApiMethod(ApiMethod.play, null)
		}

		return dialogView
	}

	// When this fragment is destroyed when rotating the device this will save the current state
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("jsonUrl", jsonUrl)
//		Logger.d("PlayerDialogFragment", "get current time ${player.getCurrentTime()}")
		outState.putDouble("offset", seekOffset)
	}

	override fun didTriggerMediaClipLoaded(playerView: BBNativePlayerView, clipData: MediaClip?) {
		super.didTriggerMediaClipLoaded(playerView, clipData)
		var dar = clipData?.dar ?: "16:9"
		val regex = Regex("\\d+:\\d+")
		if (!dar.contains(regex)) {
			dar = "16:9"
		}

		val (widthString, heightString) = dar.split(':')
		val darWidth = widthString.toInt()
		val darHeight = heightString.toInt()

		val width = dialogView.width + 32
		val height = dialogView.height + 32

		Logger.d("PlayerDialogFragment", "width: ${width} height: ${height} darWidth: $darWidth darHeight: $darHeight")

		if (height > width && darWidth > darHeight) {
			// Portrait view and landscape video handling
			val dialogHeight = width / darWidth * darHeight
			Logger.d("PlayerDialogFragment", "Dialog calculated height: $dialogHeight")

			val params = playerContainer.layoutParams
			params.height = dialogHeight

		} else if (width > height && darHeight > darWidth) {
			// Landscape view and portrait video handling
			val dialogWidth = height / darHeight * darWidth
			Logger.d("PlayerDialogFragment", "Dialog calculated width: $dialogWidth")

			val params = playerContainer.layoutParams
			params.width = dialogWidth
		}

		playerContainer.visibility = View.VISIBLE
	}

	override fun didTriggerSeeked(playerView: BBNativePlayerView, seekOffset: Double?) {
		super.didTriggerSeeked(playerView, seekOffset)
		this.seekOffset = seekOffset ?: 0.0
	}

	override fun onDestroyView() {
		Logger.d("PlayerDialogFragment", "onDestroyView")
		player.callApiMethod(ApiMethod.pause, null)
		playerContainer.removeAllViews()
		super.onDestroyView()
	}

	override fun onDestroy() {
		player.destroy()
		super.onDestroy()
	}
}