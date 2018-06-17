package com.jianchi.fsp.buddhismnetworkradio.tools;

import com.jianchi.fsp.buddhismnetworkradio.xmlbean.*;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//返回值有可能是null
public class AmtbQuery {

    static CacheOKHttp cacheOKHttp;
    static GsonXml gsonXml;

    public static void initOkHttpClient(CacheOKHttp http){
        cacheOKHttp = http;
    }

    static {
        XmlParserCreator parserCreator = new XmlParserCreator() {
            @Override
            public XmlPullParser createParser() {
                try {
                    return XmlPullParserFactory.newInstance().newPullParser();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gsonXml = new GsonXmlBuilder()
                .setXmlParserCreator(parserCreator)
                .setSameNameLists(true)
                .create();
    }

    public static CategoryListResult queryCategoryListResult(){
        return query("http://www.amtb.tw/app/unicast2xml.asp?act=1", CategoryListResult.class);
    }

    public static SubCategoryListResult querySubCategoryListResult(int amtbid){
        return query("http://www.amtb.tw/app/unicast2xml.asp?act=2&amtbid="+String.valueOf(amtbid), SubCategoryListResult.class);
    }

    public static List<ProgramListResult> queryProgramListResult(int amtbid){
        List<ProgramListResult> programListResults = new ArrayList<>();
        SubCategoryListResult subCategoryListResult = querySubCategoryListResult(amtbid);
        if(subCategoryListResult!=null){
            for(SubCategoryListItem item : subCategoryListResult.getList().getItem()){
                ProgramListResult programListResult = queryProgramListResult(amtbid, item.getSubamtbid());
                if(programListResult!=null)
                    programListResults.add(programListResult);
            }
        }
        return programListResults;
    }

    public static ProgramListResult queryProgramListResult(int amtbid, int subamtbid) {
        //
        String url = "http://www.amtb.tw/app/unicast2xml.asp?act=3&amtbid=" + String.valueOf(amtbid) + "&subamtbid=" + String.valueOf(subamtbid);
        ProgramListResult programListResult = query(url, ProgramListResult.class);
        if (programListResult != null) {
            programListResult.setSubamtbid(subamtbid);
            programListResult.setAmtbid(amtbid);
            for(ProgramListItem item : programListResult.getList().getItem()){
                item.setAmtbid(amtbid);
                item.setSubamtbid(subamtbid);
            }
        }
        return programListResult;
    }

    public static MediaListResult queryMediaListResult(int amtbid, int subamtbid, int lectureid, int volid){
        MediaListResult mediaListResult = query("http://www.amtb.tw/app/unicast2xml.asp?act=4&amtbid="+String.valueOf(amtbid)+"&subamtbid="+String.valueOf(subamtbid)+"&lectureid="+String.valueOf(lectureid)+"&volid="+String.valueOf(volid), MediaListResult.class);
        if (mediaListResult != null) {
            mediaListResult.setSubamtbid(subamtbid);
            mediaListResult.setAmtbid(amtbid);
            mediaListResult.setLectureid(lectureid);
        }
        return mediaListResult;
    }

    public static MediaListResult queryMediaListResult(int amtbid, int subamtbid, int lectureid){
        MediaListResult mediaListResult = query("http://www.amtb.tw/app/unicast2xml.asp?act=4&amtbid="+String.valueOf(amtbid)+"&subamtbid="+String.valueOf(subamtbid)+"&lectureid="+String.valueOf(lectureid), MediaListResult.class);
        if (mediaListResult != null) {
            mediaListResult.setSubamtbid(subamtbid);
            mediaListResult.setAmtbid(amtbid);
            mediaListResult.setLectureid(lectureid);
        }
        return mediaListResult;
    }

    /*
    private static String downHtml(String url){
        String xmlStr = "";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)//请求接口。如果需要传参拼接到接口后面。
                .build();//创建Request 对象
        Response response = null;
        try {
            response = client.newCall(request).execute();//得到Response 对象
            if (response.isSuccessful()) {
                xmlStr = new String(response.body().bytes(), "BIG5");
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return xmlStr;
    }
*/
    public static <T> T query(String urlStr, Class<T> clazz) {
        String xml = cacheOKHttp.takeXML(urlStr);
        if(xml.isEmpty()){
            return null;
        } else {
            xml = xml.replace("&nbsp", " ").replace("&", "&amp;");
            try {
                return gsonXml.fromXml(xml, clazz);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
