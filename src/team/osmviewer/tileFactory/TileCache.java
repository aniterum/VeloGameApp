/********************************************************************************
 *
 * Open Street Map Viewer v0.1
 *
 * javierbriones.info
 *
 ********************************************************************************
 *      						L I C E N S E
 ********************************************************************************
 *
 * Creative Commons (CC BY-NC 3.0) http://creativecommons.org/licenses/by-nc/3.0/
 * Francisco Javier Briones Rodriguez - 2012 
 *  
 * 
 ********************************************************************************
 * 		A U T H O R S
 ********************************************************************************
 *
 * Initials       	Name								Mail - Web
 * ---------        -----------------------------		-------------------------
 * jvbriones		F. Javier Briones Rodriguez			javierbriones.info
 * 					Saqib Hanif
 * 					Julia Amaya 
 *     
 *                                 
 ********************************************************************************
 *		R E V I S I O N   H I S T O R Y
 ********************************************************************************
 *
 * Date        	Author  	  Description
 * ---------    ---------  	  ---------------------------------------------------
 * JUN 12		team		  Initial Version v0.1
 *
 *
 *******************************************************************************/
package team.osmviewer.tileFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import team.osmviewer.activities.OpenStreetMapViewerActivity;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

//http://docs.oracle.com/javase/1.4.2/docs/api/java/util/LinkedHashMap.html --> resource for cache implementation.

public class TileCache {
	
	private Map<String, Bitmap> cacheLRU;
	private int MAX_ENTRIES;
	
	public static Bitmap getTileBitmap;
	public static Cursor cursor;
	public static String cacheKey;
	public static byte[] buffer = new byte[65536];
	
	public static SQLiteDatabase base;
	public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS TILES (ID INTEGER PRIMARY KEY AUTOINCREMENT, KEY UNSIGNED LONG, DATA BLOB)";
	public static String GET_BITMAP_BY_ID = "SELECT DATA FROM TILES WHERE ID=";
	public static String GET_BITMAP_ID_BY_KEY = "SELECT ID FROM TILES WHERE KEY=";
	public static String GET_TILE_BY_KEY = "SELECT DATA FROM TILES WHERE KEY=";
	public static String dbPath;
	public static Resources res;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TileCache(int cacheSize, String dbPath, Resources res) {
		MAX_ENTRIES = cacheSize;
		//Constructs a new LinkedHashMap instance with the specified capacity, 
		//load factor(default value is .75F) and a flag specifying the ordering behavior (true->based on the last access)
		cacheLRU = new LinkedHashMap(MAX_ENTRIES+1, .75F, true) {			
			private static final long serialVersionUID = 1L;
			
			// This method is called just after a new entry has been added (i.e after a put() call)
			// and delete the least reference element in the map if is needed.
			public boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_ENTRIES;
			}
		};
		
		TileCache.dbPath = dbPath;
		TileCache.res = res;
		
		Log.i("", "Loading Database from " + dbPath);
		
		Boolean db_exists = new File(dbPath).exists();
		// Проверка на существование файла
		if (db_exists) {
			try {
				base = SQLiteDatabase.openDatabase(OpenStreetMapViewerActivity.dbPath, null, SQLiteDatabase.OPEN_READWRITE);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			// База данных не существует. Либо это первый запуск либо чистили
			// данные приложения.

			CopyDatabaseFromResources(dbPath, res);
			base = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

		}

	}
	
	
	public Bitmap getTile(final Tile tile) {

		cacheKey = Tile.getCacheKey(tile);
		
		getTileBitmap = cacheLRU.get(cacheKey);
		if (getTileBitmap != null)
			return getTileBitmap;
		else {
			//В кэше нет этого тайла, грузим из базы данных
			try {
				cursor = base.rawQuery(GET_TILE_BY_KEY + cacheKey, null);
				if (cursor.moveToFirst()) {
					buffer = cursor.getBlob(0);
					final Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
					cacheLRU.put(cacheKey, result);
					return result;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		// Если нет тайла и в базе данных тоже
		return null;

	}
	
	
	public boolean hasTile(final Tile tile) {
		cacheKey = Tile.getCacheKey(tile);

		if (cacheLRU.get(cacheKey) != null)
			return true;
		else {

			try {
				cursor = base.rawQuery(GET_BITMAP_ID_BY_KEY + cacheKey, null);

				if (cursor.moveToFirst()) 
					return true;

			} catch (SQLiteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void put(final Tile tile, InputStream is){ //receives the stream from http (this method is called by TileManager)
		cacheLRU.put(Tile.getCacheKey(tile), BitmapFactory.decodeStream(is));
	}	
	
	private void CopyDatabaseFromResources(final String dbPath, Resources res){
		
		File db_file = new File(dbPath);
		//Открываем исходную базу данных в ресурсах
		InputStream resBase = res.openRawResource(OpenStreetMapViewerActivity.tilesID);

		try {

			FileOutputStream saveBase = new FileOutputStream(db_file);
			final int bufSize = resBase.available(); //Длина файла базы данных в ресурсах

			final byte[] buffer = new byte[bufSize];
			resBase.read(buffer);

			saveBase.write(buffer);

			resBase.close();
			saveBase.close();

		} // try
		  catch (FileNotFoundException e) {
			 e.printStackTrace();
		} catch (IOException e) {
			 e.printStackTrace();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		
	}
	
}
