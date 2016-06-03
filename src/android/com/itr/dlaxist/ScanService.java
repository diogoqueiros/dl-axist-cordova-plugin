package com.itr.dlaxist;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;

import java.util.ArrayList;

public class ScanService extends CordovaPlugin {

    protected ScanCallback<BarcodeScan> scanCallback;

    private final String LOGTAG = "DatalogicBarcodeScannerPlugin";

    BarcodeManager decoder = null;
    ReadListener listener = null;

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        if ("register".equals(action)) {
            scanCallback = new ScanCallback<BarcodeScan>() {
                @Override
                public void execute(BarcodeScan scan) {
                    Log.i(LOGTAG, "Scan result [" + scan.LabelType + "-" + scan.Barcode + "].");

                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", scan.LabelType);
                        obj.put("barcode", scan.Barcode);
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    } catch(JSONException e){
                        Log.e(LOGTAG, "Error building json object", e);

                    }
                }
            };
        }
        else if ("start".equals(action)){
            // If the decoder instance is null, create it.
            if (decoder == null) { // Remember an onPause call will set it to null.
                decoder = new BarcodeManager();
            }

            try {

                // Create an anonymous class.
                listener = new ReadListener() {

                    // Implement the callback method.
                    @Override
                    public void onRead(DecodeResult decodeResult) {
                        new AsyncDataUpdate().execute(decodeResult);
                    }

                };

                decoder.addReadListener(listener);

            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error while trying to bind a listener to BarcodeManager", e);
            }

        }
        else if ("trigger".equals(action)){
            if (scanCallback != null){
                scanCallback.execute(new BarcodeScan("UPCA", "000000000010"));
            }
        }

        return true;
    }

    private class AsyncDataUpdate extends AsyncTask<DecodeResult, Void, BarcodeScan> {

        @Override
        protected BarcodeScan doInBackground(DecodeResult... params) {

            BarcodeScan barcode = null;

            try {

                DecodeResult decodeResult = params[0];

                String str = "";

                if (decodeResult != null){
                    String str1 = decodeResult.getText();
                    if (str1 != null){
                        str = str1;
                    }
                }
                barcode = new BarcodeScan("UPC", str);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Return result to populate on UI thread
            return barcode;
        }

        @Override
        protected void onPostExecute(BarcodeScan barcode) {
            if (barcode != null && scanCallback != null){
                scanCallback.execute(barcode);
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }


    }

}
