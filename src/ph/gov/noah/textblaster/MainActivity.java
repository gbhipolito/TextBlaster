package ph.gov.noah.textblaster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ph.gov.noah.textblaster.database.TextBlasterDAO;
import ph.gov.noah.textblaster.database.tables.ContactsTable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	final private String SMS_SENT = "ph.gov.noah.sms_sent";
	final private String EXTRA_MESSAGE = "message";
	final private String EXTRA_RECIPIENT_NUMBER = "recipient";
	final private String EXTRA_RECIPIENT_NAME = "recipient";

	private Button btnSend;
	private EditText etMsg;
	private Button btnAdd;
	private ListView lvContacts;
	private Button btnMsgSender;
	private Button btnContacts;
	private Button btnHistory;
	private Button btnImport;
	private ListView lvHistory;
	private ArrayAdapter<String> aaContacts;
	private ArrayList<String> alContacts = new ArrayList<>();
	private String newContact;
	private String newNumber;
	private ArrayAdapter<String> aaHistory;
	private ArrayList<String> alHistory = new ArrayList<>();
	
	private final Object sendingLock = new Object();

	final private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (sendingLock) {
				String currDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
				TextBlasterDAO tbDao = new TextBlasterDAO(MainActivity.this);
				tbDao.open();
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
					Log.e("asdf", "for history: " + currDate + ", " + intent.getStringExtra(EXTRA_RECIPIENT_NAME) + intent.getStringExtra(EXTRA_RECIPIENT_NUMBER) + intent.getStringExtra(EXTRA_MESSAGE));
					tbDao.insertHistory(currDate,
							intent.getStringExtra(EXTRA_RECIPIENT_NAME),
							intent.getStringExtra(EXTRA_RECIPIENT_NUMBER),
							intent.getStringExtra(EXTRA_MESSAGE), "Success");
					Log.e("asdf", "saved");
					break;
				 case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			        Toast.makeText(context,"SMS Sending Error :Generic failure" ,Toast.LENGTH_SHORT).show();
					tbDao.insertHistory(currDate,
							intent.getStringExtra(EXTRA_RECIPIENT_NAME),
							intent.getStringExtra(EXTRA_RECIPIENT_NUMBER),
							intent.getStringExtra(EXTRA_MESSAGE), "Failed");
			        break;
			    case SmsManager.RESULT_ERROR_NO_SERVICE:
			        Toast.makeText(context,"SMS Sending Error :No service" ,Toast.LENGTH_SHORT).show();
					tbDao.insertHistory(currDate,
							intent.getStringExtra(EXTRA_RECIPIENT_NAME),
							intent.getStringExtra(EXTRA_RECIPIENT_NUMBER),
							intent.getStringExtra(EXTRA_MESSAGE), "Failed");
			        break;
			    case SmsManager.RESULT_ERROR_NULL_PDU:
			        Toast.makeText(context,"SMS Sending Error :Null PDU" ,Toast.LENGTH_SHORT).show();
					tbDao.insertHistory(currDate,
							intent.getStringExtra(EXTRA_RECIPIENT_NAME),
							intent.getStringExtra(EXTRA_RECIPIENT_NUMBER),
							intent.getStringExtra(EXTRA_MESSAGE), "Failed");
			        break;
			    case SmsManager.RESULT_ERROR_RADIO_OFF:
			        Toast.makeText(context,"SMS Sending Error :Radio off" ,Toast.LENGTH_SHORT).show();
					tbDao.insertHistory(currDate,
							intent.getStringExtra(EXTRA_RECIPIENT_NAME),
							intent.getStringExtra(EXTRA_RECIPIENT_NUMBER),
							intent.getStringExtra(EXTRA_MESSAGE), "Failed");
			        break;
				}
				tbDao.close();
				sendingLock.notify();
			} // synchronized (sendingLock)
		} // onReceive
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnSend = (Button) findViewById(R.id.btn_send);
		etMsg = (EditText) findViewById(R.id.et_msg);
		lvContacts = (ListView) findViewById(R.id.lv_contacts);
		btnAdd = (Button) findViewById(R.id.btn_add);
		btnMsgSender = (Button) findViewById(R.id.btn_messagesender);
		btnContacts = (Button) findViewById(R.id.btn_contacts);
		btnHistory = (Button) findViewById(R.id.btn_history);
		btnImport = (Button) findViewById(R.id.btn_import);
		lvHistory = (ListView) findViewById(R.id.lv_history);

		refreshContactsList();

		registerReceiver(smsSentReceiver, new IntentFilter(SMS_SENT));

		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!etMsg.getText().toString().trim().equals("")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setCancelable(true);
					builder.setTitle("Send Message");
					builder.setMessage("Message will be broadcasted to ALL contacts.\nAre you sure?");
					builder.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// sendMessage("09064669069",
									// etMsg.getText().toString().trim());
									final Dialog sendingDialog = new Dialog( MainActivity.this);
									sendingDialog.setContentView(R.layout.dialog_cancel);
									sendingDialog.setTitle("Add contact");
									sendingDialog.setCancelable(false);
									final Button btnCancel = (Button) sendingDialog.findViewById(R.id.cncl_btn_cancel);

									dialog.dismiss();
									sendingDialog.show();

									final MessageSendingTask mst = new MessageSendingTask(alContacts, etMsg.getText().toString().trim(), sendingDialog);

									btnCancel.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View v) {
													mst.cancel(true);
													Toast.makeText(MainActivity.this, "Sending canceled!", Toast.LENGTH_LONG).show();
													sendingDialog.dismiss();
												}
											});

									mst.execute();
								}
							});
					builder.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});

					final AlertDialog dialog = builder.create();
					dialog.show();
				} // if( !etMsg.getText().toString().trim().equals("") )
			} // onClick
		}); // setlistener

		btnMsgSender.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnMsgSender.setEnabled(false);
				btnContacts.setEnabled(true);
				btnHistory.setEnabled(true);

				btnSend.setVisibility(View.VISIBLE);
				etMsg.setVisibility(View.VISIBLE);
				lvContacts.setVisibility(View.GONE);
				btnAdd.setVisibility(View.GONE);
				btnImport.setVisibility(View.GONE);
				lvHistory.setVisibility(View.GONE);
			}
		});

		btnContacts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnMsgSender.setEnabled(true);
				btnContacts.setEnabled(false);
				btnHistory.setEnabled(true);

				btnSend.setVisibility(View.GONE);
				etMsg.setVisibility(View.GONE);
				lvContacts.setVisibility(View.VISIBLE);
				btnAdd.setVisibility(View.VISIBLE);
				btnImport.setVisibility(View.VISIBLE);
				lvHistory.setVisibility(View.GONE);

				aaContacts.notifyDataSetChanged();
			}
		});

		btnHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnMsgSender.setEnabled(true);
				btnContacts.setEnabled(true);
				btnHistory.setEnabled(false);

				btnSend.setVisibility(View.GONE);
				etMsg.setVisibility(View.GONE);
				lvContacts.setVisibility(View.GONE);
				btnAdd.setVisibility(View.GONE);
				btnImport.setVisibility(View.GONE);
				lvHistory.setVisibility(View.VISIBLE);

				refreshHistory();
			}
		});

		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addContact();
			}
		});

		btnImport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setCancelable(true);
				builder.setTitle("Import Contacts From File");
				builder.setMessage("This will REMOVE ALL non-default contacts then import new ones. Default contacts start with ~."
						+ "Imports will be coming from contacts_to_import.txt. It can not be undone.\nARE YOU SURE?");
				builder.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								new ImportTask().execute();
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});

				final AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		lvContacts.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> list,
					View child, final int pos, long id) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setCancelable(true);
				builder.setTitle("Delete Contact");
				builder.setMessage("Are you sure?");
				builder.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								String text = ((TextView) list.getChildAt(pos)).getText().toString();
								Log.e("asdf", "del: " + text.substring(text.lastIndexOf(":") + 1).trim());
								deleteContact(text.substring(text.lastIndexOf(":") + 1).trim());
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});

				final AlertDialog dialog = builder.create();
				dialog.show();
				return true;
			}
		});
	} // onCreate

	// private void sendMessage(String number, String message) {
	// try {
	// SmsManager smsManager = SmsManager.getDefault();
	// smsManager.sendTextMessage(number, null, message, null, null);
	// Toast.makeText(getApplicationContext(), "SMS sent.",
	// Toast.LENGTH_LONG).show();
	// } catch (Exception e) {
	// Toast.makeText(getApplicationContext(),
	// "SMS failed, please try again.",
	// Toast.LENGTH_LONG).show();
	// e.printStackTrace();
	// }
	// }

	private void refreshContactsList() {
		TextBlasterDAO tbDAO = new TextBlasterDAO(this);
		tbDAO.open();
		alContacts = tbDAO.getContacts();
		Log.e("asdf", "alcontacts: " + alContacts);
		for (String a : alContacts) {
			Log.e("asdf", "asdf: " + a);
		}
		aaContacts = new ArrayAdapter<>(getBaseContext(),
				android.R.layout.simple_list_item_1, alContacts);
		lvContacts.setAdapter(aaContacts);
		tbDAO.close();
	}

	private void refreshHistory() {
		TextBlasterDAO tbDAO = new TextBlasterDAO(this);
		tbDAO.open();
		alHistory = tbDAO.getHistory();
		Log.e("asdf", "alhistory: " + alHistory);
		for (String a : alHistory) {
			Log.e("asdf", "asdf: " + a);
		}
		aaHistory = new ArrayAdapter<>(getBaseContext(),
				android.R.layout.simple_list_item_1, alHistory);
		lvHistory.setAdapter(aaHistory);
		tbDAO.close();
	}

	private void addContact() {
		newContact = "";
		newNumber = "";

		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.dialog_add);
		dialog.setTitle("Add contact");

		final EditText etName = (EditText) dialog
				.findViewById(R.id.dlg_et_name);
		final EditText etNum = (EditText) dialog.findViewById(R.id.dlg_et_num);
		final Button btnAdd = (Button) dialog.findViewById(R.id.dlg_btn_add);
		final Button btnCancel = (Button) dialog
				.findViewById(R.id.dlg_btn_cancel);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newContact = etName.getText().toString();
				newNumber = etNum.getText().toString();

				if (!newContact.trim().equals("")
						&& !newNumber.trim().equals("")) {
					TextBlasterDAO tbDAO = new TextBlasterDAO(MainActivity.this);
					tbDAO.open();
					tbDAO.insertContact(newContact, newNumber);
					tbDAO.close();

					dialog.dismiss();
					refreshContactsList();
				} // if( !newContact.trim().equals("") &&
					// !newNumber.trim().equals("") )
			} // onClick
		});

		dialog.show();
	} // addContact

	private void deleteContact(String number) {
		TextBlasterDAO tbDAO = new TextBlasterDAO(MainActivity.this);
		tbDAO.open();
		tbDAO.deleteContact(number);
		tbDAO.close();

		refreshContactsList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(smsSentReceiver);
	}

	private class MessageSendingTask extends AsyncTask<Void, String, Void> {

		private ArrayList<String> mContacts;
		private String mMessage;
		private Dialog mDialog;
//		PendingIntent piSent;

		public MessageSendingTask(ArrayList<String> contacts, String message, Dialog dialog) {
			mContacts = contacts;
			mMessage = message;
			mDialog = dialog;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				final SmsManager smsManager = SmsManager.getDefault();
				for (String contact : mContacts) {
					synchronized (sendingLock) {
						if (isCancelled()) {
//							Toast.makeText(MainActivity.this, "Totally cancel", Toast.LENGTH_LONG).show();
							Log.e("asdf", "CANCELED!!!");
							break;
						} else {
							Log.e("asdf", "NOT CANCELED!!!");
						}
						
						Log.e("asdf", "running");
						
						String number = "";
						String name = "";
						try {
							Log.e("asdf", "send" + contact.substring(contact.lastIndexOf(":") + 1).trim());
							number = contact.substring(contact.lastIndexOf(":") + 1).trim();
							name = contact.substring(0, contact.lastIndexOf(":")).trim();
							Intent intent = new Intent(SMS_SENT);
							intent.putExtra(EXTRA_RECIPIENT_NUMBER, number);
							intent.putExtra(EXTRA_RECIPIENT_NAME, name);
							intent.putExtra(EXTRA_MESSAGE, mMessage);
							PendingIntent piSent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
							publishProgress("Sending to: " + name);
							smsManager.sendTextMessage(number, null, mMessage, piSent, null);
							// Toast.makeText(getApplicationContext(),
							// "SMS sent to: " + number, Toast.LENGTH_LONG).show();
						} catch (Exception e) {
							Log.e("asdf", "sending error: " + e.getMessage());
							// Toast.makeText(getApplicationContext(),
							// "Failed sending to: " + number,
							// Toast.LENGTH_LONG).show();
						}
						sendingLock.wait();
					} // synchronized (sendingLock)
				} // for (String contact : mContacts)
			} catch (SQLiteException e) {
				// Toast.makeText(getApplicationContext(),
				// "Failed to save to history", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (Exception e) {
				
				// Toast.makeText(getApplicationContext(),
				// "SMS failed, please try again.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			publishProgress("");
			return null;
		} // doInBackground

		@Override
		protected void onProgressUpdate(String... values) {
			if( values[0].equals("")) {
				mDialog.dismiss();
			} else {
				Toast.makeText(MainActivity.this, values[0], Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			mDialog.dismiss();
		}

	} // MessageSendingTask

	private class ImportTask extends AsyncTask<Void, Void, Void> {

		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPreExecute() {
			dialog.setTitle("Importing contacts");
			dialog.setCancelable(false);

			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			TextBlasterDAO tbDao = new TextBlasterDAO(MainActivity.this);
			tbDao.open();
			tbDao.deleteNotDefaultContacts();
			tbDao.close();
			try {
				final RandomAccessFile rafImports = new RandomAccessFile(Environment.getExternalStorageDirectory() + "/contacts_to_import.txt", "r");
				Log.e("asdf", "import path: " + Environment.getExternalStorageDirectory() + "/contacts_to_import.txt");
				ArrayList<ContentValues> alImports = new ArrayList<>();
				ContentValues cv = new ContentValues();
				String line = "";
				String name = "";
				String number = "";
				while ((line = rafImports.readLine()) != null && !line.trim().equals("")) {
					try {
						Log.e("asdf", "Line is: " + line);
						name = line.substring(0, line.lastIndexOf(":")).trim();
						Log.e("asdf", "Name is: " + name);
						number = line.substring(line.lastIndexOf(":") + 1).trim();
						Log.e("asdf", "Number is: " + number);
						cv = new ContentValues();
						cv.put(ContactsTable.STRCOLNAME, name);
						cv.put(ContactsTable.STRCOLNUMBER, number);
						alImports.add(cv);
					} catch(Exception e) {
						Log.e("asdf", "error importing 1 contact: " + e.getMessage());
					}
				}
				tbDao = new TextBlasterDAO(MainActivity.this);
				tbDao.open();
				tbDao.insertMultipleContacts(alImports);
				tbDao.close();

				rafImports.close();
			} catch (FileNotFoundException e) {
				Log.e("asdf", "contacts_to_import.txt not found: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("asdf", "import err: " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshContactsList();
			dialog.dismiss();
		}

	} // ImportTask

} // MainActivity