package ph.gov.noah.textblaster.database;

import java.util.ArrayList;

import ph.gov.noah.textblaster.database.tables.ContactsTable;
import ph.gov.noah.textblaster.database.tables.HistoryTable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class TextBlasterDAO {

	private Context parentContext;
	private SQLiteDatabase textBlasterDatabase;
	private TextBlasterDatabaseHelper textBlasterDatabaseHelper;

	public TextBlasterDAO(Context _context) {
		parentContext = _context;
		textBlasterDatabaseHelper = new TextBlasterDatabaseHelper(parentContext);
	}

	public void open() {
		textBlasterDatabase = textBlasterDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		textBlasterDatabaseHelper.close();
		textBlasterDatabase.close();
	}

	public ArrayList<String> getContacts() {

		ArrayList<String> alContacts = new ArrayList<String>();
		
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();

			Cursor cursor = null;
			try {
//				textBlasterDatabase.execSQL("drop table tbl_contacts");
				cursor = textBlasterDatabase.query(ContactsTable.STRTABLENAME, ContactsTable.ARRALLCOLUMNS, null, null, null, null, ContactsTable.STRCOLNAME);
				Log.e("asdf", "cursor size: " + cursor.getCount());
				// put query results to int array
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						alContacts.add(cursor.getString(0) + ":   " + cursor.getString(1));
						cursor.moveToNext();
					}
					cursor.close();
				}
				
				textBlasterDatabase.setTransactionSuccessful();
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
			} finally {
				textBlasterDatabase.endTransaction();
			}

		} // if (textBlasterDatabase != null)

		return alContacts;
	} // getContacts
	
	public boolean insertContact( String name, String number ) {
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();
			Log.e("asdf", "insert contact!");
			try {
				ContentValues cv = new ContentValues();
				Log.e("asdf", "name: " + name + " value: " + number);
				cv.put(ContactsTable.STRCOLNAME, name);
				cv.put(ContactsTable.STRCOLNUMBER, number);
				textBlasterDatabase.insertWithOnConflict(ContactsTable.STRTABLENAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
				Log.e("asdf", "values inserted!");
				textBlasterDatabase.setTransactionSuccessful();
				return true;
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
				return false;
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
				return false;
			} finally {
				textBlasterDatabase.endTransaction();
			}
		}
		return false;
	} // insertContact
	
	public boolean insertMultipleContacts( ArrayList<ContentValues> alImports ) {
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();
			Log.e("asdf", "insert contact!");
			try {
				for( ContentValues contactImport : alImports ) {
					try {
						textBlasterDatabase.insertWithOnConflict(ContactsTable.STRTABLENAME, null, contactImport, SQLiteDatabase.CONFLICT_REPLACE);
						Log.e("asdf", "values inserted!");
					} catch (SQLiteConstraintException sqlce ) {
						Log.e("asdf", "asdf: " + sqlce.getMessage());
						sqlce.printStackTrace(); // TODO replace w/ err msg
						return false;
					} catch (SQLiteException e) {
						Log.e("asdf", "asdf: " + e.getMessage());
						e.printStackTrace(); // TODO replace w/ err msg
						return false;
					} 
				}
				textBlasterDatabase.setTransactionSuccessful();
				return true;
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
				return false;
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
				return false;
			} finally {
				textBlasterDatabase.endTransaction();
			}
		}
		return false;
	} // insertMultipleContacts
	
	public boolean deleteContact( String number ) {
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();
			Log.e("asdf", "delete contact!");
			try {
				textBlasterDatabase.delete(ContactsTable.STRTABLENAME, ContactsTable.STRCOLNUMBER + " = '" + number + "'", null);
				Log.e("asdf", "del state: " + ContactsTable.STRCOLNUMBER + " = '" + number + "'");
				textBlasterDatabase.setTransactionSuccessful();
				return true;
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
				return false;
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
				return false;
			} finally {
				textBlasterDatabase.endTransaction();
			}
		}
		return false;
	} // deleteContact
	
	public boolean deleteNotDefaultContacts() {
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();
			Log.e("asdf", "delete multiple contacts!");
			try {
				textBlasterDatabase.delete(ContactsTable.STRTABLENAME, ContactsTable.STRCOLNAME + " NOT LIKE '~%'", null);
				textBlasterDatabase.setTransactionSuccessful();
				return true;
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
				return false;
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
				return false;
			} finally {
				textBlasterDatabase.endTransaction();
			}
		}
		return false;
	} // deleteNotDefaultContacts
	
	public boolean insertHistory( String timeSent, String name, String number, String message, String status ) {
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();
			Log.e("asdf", "insert contact!");
			try {
				ContentValues cv = new ContentValues();
				Log.e("asdf", "name: " + name + " value: " + number);
				cv.put(HistoryTable.STRCOLTIMESENT, timeSent);
				cv.put(HistoryTable.STRCOLNAME, name);
				cv.put(HistoryTable.STRCOLNUMBER, number);
				cv.put(HistoryTable.STRCOLMESSAGE, message);
				cv.put(HistoryTable.STRCOLSTATUS, status);
				textBlasterDatabase.insertWithOnConflict(HistoryTable.STRTABLENAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
				Log.e("asdf", "values inserted!");
				textBlasterDatabase.setTransactionSuccessful();
				return true;
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
				return false;
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
				return false;
			} finally {
				textBlasterDatabase.endTransaction();
			}
		}
		return false;
	} // insertHistory
	
	public ArrayList<String> getHistory() {

		ArrayList<String> alHistory = new ArrayList<String>();
		
		if (textBlasterDatabase != null) {
			textBlasterDatabase.beginTransaction();

			Cursor cursor = null;
			try {
				cursor = textBlasterDatabase.query(HistoryTable.STRTABLENAME, HistoryTable.ARRQUERYCOLUMNS, null, null, null, null, HistoryTable.STRCOLTIMESENT + " DESC, " + HistoryTable.STRCOLNAME + " ASC");
				Log.e("asdf", "cursor size: " + cursor.getCount());
				// put query results to int array
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						alHistory.add(cursor.getString(0) + " --;-- " + cursor.getString(1) + " --;-- " + cursor.getString(2) + "\n" + cursor.getString(3));
						cursor.moveToNext();
					}
					cursor.close();
				}
				
				textBlasterDatabase.setTransactionSuccessful();
			}catch (SQLiteConstraintException sqlce ) {
				Log.e("asdf", "asdf: " + sqlce.getMessage());
				sqlce.printStackTrace(); // TODO replace w/ err msg
			} catch (SQLiteException e) {
				Log.e("asdf", "asdf: " + e.getMessage());
				e.printStackTrace(); // TODO replace w/ err msg
			} finally {
				textBlasterDatabase.endTransaction();
			}

		} // if (textBlasterDatabase != null)

		return alHistory;
	} // getHistory

} // TextBlasterDAO