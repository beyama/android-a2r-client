package eu.addicted2random.a2rclient.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.addicted2random.a2rclient.R;

/**
 * Dialog shown in the main activity to show the user that no network connection is active
 * and let her choose between cancel, retry and open network settings.
 * 
 * The activity which is using this dialog fragment must implement
 * {@link NotConnectedDialog.NotConnectedDialogListener}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class NotConnectedDialog extends SherlockDialogFragment {

	public interface NotConnectedDialogListener {
		/**
		 * Invoked on click of the "retry" button.
		 * 
		 * @param dialog
		 */
		void onNotConnectedDialogRetry(NotConnectedDialog dialog);
		
		/**
		 * Invoked on click of the background.
		 * @param dialog
		 */
		void onNotConnectedDialogCancel(NotConnectedDialog dialog);
		
		/**
		 * Invoked on click of the "network settings" button.
		 * @param dialog
		 */
		void onNotConnectedDialogOpenSettings(NotConnectedDialog dialog);
	}

	private NotConnectedDialogListener mListener;

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockDialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (NotConnectedDialogListener) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString() + " must implement " + NotConnectedDialogListener.class.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder
				.setTitle(R.string.not_connected_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.not_connected_message)
				.setPositiveButton(R.string.not_connected_retry, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mListener.onNotConnectedDialogRetry(NotConnectedDialog.this);
					}
				})
				.setNegativeButton(R.string.not_connected_open_network_settings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mListener.onNotConnectedDialogOpenSettings(NotConnectedDialog.this);
					}
				});
		
		return builder.create();
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		
		mListener.onNotConnectedDialogCancel(this);
	}

}
