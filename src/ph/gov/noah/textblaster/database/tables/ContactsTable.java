package ph.gov.noah.textblaster.database.tables;

import ph.gov.noah.textblaster.constants.DBConstants;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ContactsTable {
	// table name and its columns
	public static final String STRTABLENAME = "tbl_contacts";
	public static final String STRCOLNAME = "contact_name";
	public static final String STRCOLNUMBER = "phone_number";
	public static final String[] ARRALLCOLUMNS = {	STRCOLNAME,
													STRCOLNUMBER };

	private static String strCreateTable = "";

	static {
		StringBuilder sbCreateTable = new StringBuilder();
		sbCreateTable.append(DBConstants.STRCREATETABLE);
		sbCreateTable.append(" ");
		sbCreateTable.append(STRTABLENAME);
		sbCreateTable.append("(");
		sbCreateTable.append(STRCOLNAME);					// name
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(",");
		sbCreateTable.append(STRCOLNUMBER);					// number
		sbCreateTable.append(DBConstants.STRSTEXT);
		sbCreateTable.append(DBConstants.STRSUNIQUE);
		sbCreateTable.append(DBConstants.STRSNOTNULL);
		sbCreateTable.append(")");

		strCreateTable = sbCreateTable.toString();
		
		Log.e("asdf", "create: " + strCreateTable);
		
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