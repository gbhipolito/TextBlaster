package ph.gov.noah.textblaster.database.tables;

import ph.gov.noah.textblaster.constants.DBConstants;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryTable {
	// table name and its columns
	public static final String STRTABLENAME = "tbl_history";
	public static final String STRCOLTIMESENT = "time_sent";
	public static final String STRCOLNAME = "contact_name";
	public static final String STRCOLNUMBER = "phone_number";
	public static final String STRCOLMESSAGE = "message";
	public static final String STRCOLSTATUS = "status";
	public static final String[] ARRQUERYCOLUMNS = {	STRCOLTIMESENT,
														STRCOLNAME,
														STRCOLSTATUS,
														STRCOLMESSAGE	};

	private static String strCreateTable = "";

	static {
		StringBuilder sbCreateTable = new StringBuilder();
		sbCreateTable.append(DBConstants.STRCREATETABLE);
		sbCreateTable.append(" ");
		sbCreateTable.append(STRTABLENAME);
		sbCreateTable.append("(");
		sbCreateTable.append(STRCOLTIMESENT);				// time sent
		sbCreateTable.append(DBConstants.STRSTIMESTAMP);
		sbCreateTable.append(DBConstants.STRSDEFAULTS);
		sbCreateTable.append(DBConstants.STRSCURRTIMESTAMP);
		sbCreateTable.append(",");
		sbCreateTable.append(STRCOLNAME);					// name
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(",");
		sbCreateTable.append(STRCOLNUMBER);					// number
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(",");
		sbCreateTable.append(STRCOLMESSAGE);				// message
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(",");
		sbCreateTable.append(STRCOLSTATUS);					// status
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(")");

		strCreateTable = sbCreateTable.toString();
		
		Log.e("asdf", "create history: " + strCreateTable);
		
	} // static

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(strCreateTable);
		Log.e("asdf", "database created!");
	} // onCreate

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DBConstants.STRDROPTABLE + " " + STRTABLENAME);
		onCreate(db);
	}
}