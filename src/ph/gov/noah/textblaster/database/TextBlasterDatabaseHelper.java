package ph.gov.noah.textblaster.database;

import ph.gov.noah.textblaster.database.tables.ContactsTable;
import ph.gov.noah.textblaster.database.tables.HistoryTable;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TextBlasterDatabaseHelper extends SQLiteOpenHelper {
	private static final String STRDATABASENAME = "textblaster.db";
	private static final int STRDATABASEVERSION = 1;

	public TextBlasterDatabaseHelper(Context context) {
		super(context, STRDATABASENAME, null, STRDATABASEVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ContactsTable.onCreate( db );
		HistoryTable.onCreate( db );
		
//		db.beginTransaction();
//		try {
//			ContentValues cv = new ContentValues();
//			
//			cv.put(ContactsTable.STRCOLNAME, "~me");
//			cv.put(ContactsTable.STRCOLNUMBER, "12345678");
//			db.insert(ContactsTable.STRTABLENAME, null, cv);
//		
//			cv.put(ContactsTable.STRCOLNAME, "~nice guy");
//			cv.put(ContactsTable.STRCOLNUMBER, "87654321");
//			db.insert(ContactsTable.STRTABLENAME, null, cv);
//			
//			db.setTransactionSuccessful();
//			Log.e("asdf", "values inserted!");
//		} catch (SQLiteException e) {
//			e.printStackTrace(); // TODO replace w/ err msg
//			Log.e("asdf", "asdf: " + e.getMessage());
//		} finally {
//			db.endTransaction();
//		}
	} // onCreate

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ContactsTable.onUpgrade( db, oldVersion, newVersion );
		HistoryTable.onUpgrade( db, oldVersion, newVersion );
	}
}