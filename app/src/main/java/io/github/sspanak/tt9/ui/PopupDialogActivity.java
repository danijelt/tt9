package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;
import io.github.sspanak.tt9.ui.dialogs.ConfirmDictionaryUpdateDialog;
import io.github.sspanak.tt9.ui.dialogs.PopupDialog;
import io.github.sspanak.tt9.util.Logger;

public class PopupDialogActivity extends AppCompatActivity {
	private static final String LOG_TAG = PopupDialogActivity.class.getSimpleName();


	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		PopupDialog dialog = getDialog();
		if (dialog != null) {
			dialog.render();
		} else {
			onDialogClose("");
		}
	}


	private PopupDialog getDialog() {
		Intent i = getIntent();

		String popupType = i != null ? i.getStringExtra(PopupDialog.PARAMETER_DIALOG_TYPE) : "";
		popupType = popupType != null ? popupType : "";

		switch (popupType) {
			case AddWordDialog.TYPE:
				return new AddWordDialog(this, i, this::onDialogClose);
			case ConfirmDictionaryUpdateDialog.TYPE:
				return new ConfirmDictionaryUpdateDialog(this, i, this::onDialogClose);
			default:
				Logger.w(LOG_TAG, "Unknown popup type: '" + popupType + "'. Not displaying anything.");
				return null;
		}
	}

	private void onDialogClose(String message) {
		finish();
		sendMessageToMain(message);
	}

	private void sendMessageToMain(String message) {
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(PopupDialog.INTENT_CLOSE, message);
		startService(intent);
	}
}
