package app.tomlai.com.firefighterreportingsystem;


import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class GeoUtil {

    public static String getAddressByLatLng(LatLng latLng) {
        String addr = "";
        // 同步
        GeoUtil.LatLngToAddress ga = new GeoUtil.LatLngToAddress(latLng);
        FutureTask<String> future = new FutureTask<String>(ga);
        new Thread(future).start();
        try {
            addr = future.get();
        } catch (Exception e) {

        }
        return addr;
    }
    public static LatLng getLatLngByAddress(String address) {
        LatLng latLng = null;
        // 同步
        GeoUtil.AddressToLatLng ga = new GeoUtil.AddressToLatLng(address);
        FutureTask<LatLng> future = new FutureTask<LatLng>(ga);
        new Thread(future).start();
        try {
            latLng = future.get();
        } catch (Exception e) {

        }
        return latLng;
    }

    private static class HttpUtil {
        public static String get(String urlString) throws Exception {
            InputStream is = null;
            Reader reader = null;
            StringBuilder str = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection URLConn = url.openConnection();
            URLConn.setRequestProperty("User-agent", "IE/6.0");
            is = URLConn.getInputStream();
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1];
            while (reader.read(buffer) != -1) {
                str.append(new String(buffer));
            }
            return str.toString();
        }
    }

    // Geo轉Address
    static class LatLngToAddress implements Callable<String> {

        private String queryURLString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true&language=zh_tw";
        private String address = "Not fund !";;
        private LatLng latLng;

        LatLngToAddress(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        public String call() {
            // 輸入緯經度得到地址
            address = getAddressByLocation();
            return address;

        }

        public String getAddressByLocation() {
            String urlString = String.format(queryURLString, latLng.latitude, latLng.longitude);
            try {
                // 取得 json string
                String jsonStr = HttpUtil.get(urlString);
                // 取得 json 根陣列節點 results
                JSONArray results = new JSONObject(jsonStr)
                        .getJSONArray("results");
                if (results.length() >= 1) {
                    // 取得 results[0]
                    JSONObject jsonObject = results.getJSONObject(0);
                    // 取得 formatted_address 屬性內容
                    address = jsonObject.optString("formatted_address");
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return address;

        }

    }

    // Address轉Geo地址
    static class AddressToLatLng implements Callable<LatLng> {
        private String queryURLString = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=false&language=zh_tw";
        private String address;

        AddressToLatLng(String address) {
            this.address = address;
        }

        @Override
        public LatLng call() {
            LatLng latLng = null;
            try {
                // // 輸入地址得到緯經度(中文地址需透過URLEncoder編碼)
                latLng = getLocationByAddress(URLEncoder.encode(address, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
            return latLng;
        }

        public LatLng getLocationByAddress(String address) {
            String urlString = String.format(queryURLString, address);
            LatLng latLng = null;
            try {
                // 取得 json string
                String jsonStr = HttpUtil.get(urlString);
                // 取得 json 根陣列節點 results
                JSONArray results = new JSONObject(jsonStr)
                        .getJSONArray("results");
                System.out.println("results.length() : " + results.length());
                if (results.length() >= 1) {
                    // 取得 results[0]
                    JSONObject jsonObject = results.getJSONObject(0);
                    // 取得 geometry --> location 物件
                    JSONObject laln = jsonObject.getJSONObject("geometry")
                            .getJSONObject("location");

                    latLng = new LatLng(Double.parseDouble(laln.getString("lat")), Double.parseDouble(laln.getString("lng")));
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return latLng;

        }

    }
}
