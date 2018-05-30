package com.runbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {

private static final String ACCESS_TOKEN = "C8mCXzvXFNAAAAAAAAABaQ9C82-fhkyosKdWMZR9t3ohpPrzu3OtUmjWcKd8w-qk";
private static String LOCAL_SESSIONS_DIR = ".//Sessions/";
private static String DROPBOX_SESSIONS_DIR = "/приложения/WahooFitness";
private static String LOG_FILE = "log.txt";

public static void main(String[] args) throws IOException, DbxException {

LOG_FILE = args[0] + LOG_FILE;
LOCAL_SESSIONS_DIR = args[0] + LOCAL_SESSIONS_DIR;

DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

Log.addText("Begin", LOG_FILE);

ListFolderResult folders = client.files().listFolder(DROPBOX_SESSIONS_DIR);
for (int i = 0; i < folders.getEntries().size(); i++) {
String curDir = DROPBOX_SESSIONS_DIR + "/" + folders.getEntries().get(i).getName();
String fileName = client.files().listFolder(curDir).getEntries().get(0).getName();
String filePath = curDir + "/" + fileName;

File GH = new File(LOCAL_SESSIONS_DIR);
if (!GH.exists()){
GH.mkdir();
}

String fileLocal = LOCAL_SESSIONS_DIR + fileName;
FileOutputStream f = new FileOutputStream(fileLocal);

client.files().downloadBuilder(filePath).download(f);

Log.addText("File: " + fileLocal + " download from DropBox", LOG_FILE);

training_File tf = new training_File(fileLocal);
boolean Result = Runtastic.upload(tf.getName(),
tf.getSize(),
tf.getBody(),
Runtastic.getauthenticity_token(),
LOG_FILE);
if(Result){
client.files().delete(curDir);
Log.addText("File: " + tf.getName() + " was uploaded to Runtastic", LOG_FILE);
}else{
Log.addText("File: " + tf.getName() + " wasn't uploaded to Runtastic", LOG_FILE);
}

}

Log.addText("End", LOG_FILE);

}
}

class training_File{

private String name;
private long size;
private String body;

// constructor
public training_File(String Name){
File F = new File(Name);

this.name = Name;
this.size = F.length();
}

public long getSize(){
return this.size;
}

public String getName(){
File F = new File(this.name);
return F.getName(); 
}

public String getBody() throws IOException {

StringBuilder body = new StringBuilder();

try {
//Объект для чтения файла в буфер
BufferedReader in = new BufferedReader(new FileReader(this.name));
try {
//В цикле построчно считываем файл
String s;
while ((s = in.readLine()) != null) {
body.append(s);
body.append("\n");
}
} finally {
//Также не забываем закрыть файл
in.close();
}
} catch (IOException e) {
throw new RuntimeException(e);
}

return body.toString();
}


}

class Runtastic {

static boolean upload(String FileName, long FileSize, String FileBody, String AuthToken, String logfile) throws IOException {

String url = "https://www.runtastic.com/import/upload_session?authenticity_token=%AuthToken%&qqfile=%FileName%";;
url = url.replaceAll("%FileName%", FileName);
url = url.replaceAll("%AuthToken%", AuthToken);

Connection conn = Jsoup.connect(url);
conn.headers(getCommonHeaders());
conn.requestBody(FileBody);
conn.post();

boolean Result = false;

if(conn.response().statusCode() == 200){

String body = conn.response().body();

JsonParser jp = new JsonParser();
JsonObject jo = jp.parse(body).getAsJsonObject();

for (Map.Entry entry: jo.entrySet()) {
if(entry.getKey().toString()=="success"){
Result = true;
}
Log.addText("Reason of uploading file" + FileName + " is " + entry.getValue().toString(), logfile);
break;
}

}else{
Log.addText("Code of uploading file" + FileName + " is " + conn.response().statusCode(), logfile);
}

return Result;

}
private static Map<String, String> getCommonHeaders(){

Map<String, String> map = new HashMap<String, String>();
map.put("Accept", "*/*");
// map.put("Accept-Encoding", "gzip, deflate, br");
map.put("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
map.put("Connection", "Keep-Alive");
map.put("Cookie", "__gads=ID=a8c9d470731c8f75:T=1472638409:S=ALNI_MZEjDJzWxryEjuzYvmCL_b_2WVCpA; optimizelyEndUserId=oeu1480408791052r0.4094335678433958; split=%7B%22navigation_100%22%3A%22new%22%2C%22premium_banner_version%22%3A%22original%22%7D; __utma=235500034.1683785991.1472638400.1487663606.1493104303.3; __utmz=235500034.1486018693.1.1.utmcsr=help.runtastic.com|utmccn=(referral)|utmcmd=referral|utmcct=/hc/en-us/requests/new; bl_utm_content=blogpost; bl_utm_source=runtastic; bl_utm_medium=email; bl_utm_campaign=newsletter; bl_utm_term=2017-07-19; optimizelySegments=%7B%227554022692%22%3A%22referral%22%2C%227560872113%22%3A%22gc%22%2C%227561754580%22%3A%22false%22%2C%227568292092%22%3A%22none%22%2C%227862821956%22%3A%22ru%22%7D; optimizelyBuckets=%7B%7D; fbm_162918433202=base_domain=.runtastic.com; __utmt=1; fbsr_162918433202=oR_kL95FCKiHZn4x1N9X1DzrTOdLpJzlK_gHA8kG6Ew.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUQ2TF9vS2p3Y2JHTW1tZm5YX1hyS3k3M1BBWVY1MmlTMTdTRklzaDdWMFJHV2U0ZUM2TWpQcVpMbWFkTFYzTVN2ZUxpT1A4UGQzQXNVOHlsaFNUOG5jbDlKOGw5U3c0VlkwZnFaQ3FHenJob0FMa0piVG5xUUZNMjdtb0RWOW9hREZVdGlBdHlfVzZVenIxNGp3SVhIXzV1VFNBRkxjQkdLS1VjMXhfbEZrb1VIcUZqUFRFQWVZZVVaYWQ4UzFtcHRLanhlV2pMTzBDLW5tUkNWOG4zY1VXZlZucUkxZHhyLU1FS1cwSWphRHk3Y1pYQW04MEdkMU1JdXF6YjlHYU85SkxkczQ5OVdkaDJRNnJlZWNtbkRENFNURXlJTFU4V05KejJTSXFOWjJDdU5rZXR2aDAwLTFSVk1HRXFQcWNZa0pweWZvN3ZVSS1nZEhpRDZFMW9VXyIsImlzc3VlZF9hdCI6MTUwMzA2NjM1MCwidXNlcl9pZCI6IjE3NDIxNjg3MDYxMTIyMzgifQ; _gat=1; si=1c36178a-16d0-4737-a045-d88bef8bfb09; sisa=1503065567000; seco=15; ut=a6b89ace0e8ab2aa819981569130f38bc3f24e69; at=61edff63-2e8b-429b-8a34-af85cb3ac087; __utma=1.1683785991.1472638400.1502190168.1503065567.96; __utmb=1.42.8.1503066588108; __utmc=1; __utmz=1.1488435957.50.2.utmcsr=link.runtastic.com|utmccn=(referral)|utmcmd=referral|utmcct=/; _mkra_ctxt=2783d21e137f80da754d1fc1879ed58c--200; locale=ru; _runtastic_session=BAh7CUkiD3Nlc3Npb25faWQGOgZFVEkiJTcxMTJjZDE4ZTMzMDk1MDFhYWU3ZDhlOGViZTA0ZmMwBjsAVEkiE3VzZXJfcmV0dXJuX3RvBjsAVCJCL3J1L3VzZXJzLzczMTA5ZWE5LTYwMWItOGE0NC03YjAwLWE2Zjc3MjRlMjljNS9pbXBvcnRfaWZyYW1lP0kiEF9jc3JmX3Rva2VuBjsARkkiMUFpNjZOMWd6U1RLeUt3TlpUTDZOWjRTaWl2MWZ2WXFrNW9lNWUrcndaUkk9BjsARkkiGXdhcmRlbi51c2VyLnVzZXIua2V5BjsAVFsISSIJVXNlcgY7AFRbBmkEJEBWA0kiFHNlcmdleS10cnVzb3YtMgY7AFQ%3D--6bdd4fef927194347fa1d143a331239cab89ccf1; _ga=GA1.2.1683785991.1472638400; _gid=GA1.2.1607257957.1503066664");
map.put("Host", "www.runtastic.com";);
map.put("Origin", "https://www.runtastic.com";);
map.put("Referer", "https://www.runtastic.com/ru/users/73109ea9-601b-8a44-7b00-a6f7724e29c5/import_iframe";);
map.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
map.put("X-NewRelic-ID", "Vw4AU1VACwMFUVVRAAA=");
map.put("X-Requested-With", "XMLHttpRequest");

return map;

}
static String getauthenticity_token() throws IOException{

Connection conn = Jsoup.connect("https://www.runtastic.com/";);
conn.headers(getCommonHeaders());
Document doc = conn.get();

String csrf = doc.getElementsByAttributeValue("name", "csrf-token").attr("content");
String csrf_encode = URLEncoder.encode(csrf, "UTF-8");

return csrf_encode;
}
static boolean test(String FileName, long FileSize, String FileBody, String AuthToken) throws IOException {

String url = "https://www.runtastic.com/import/upload_session?authenticity_token=%AuthToken%&qqfile=%FileName%";;
url = url.replaceAll("%FileName%", FileName);
url = url.replaceAll("%AuthToken%", AuthToken);

Connection conn = Jsoup.connect(url);
conn.headers(getCommonHeaders());
conn.requestBody(FileBody);
conn.post();

boolean Result = false;

if(conn.response().statusCode() == 200){

String body = conn.response().body();

JsonParser jp = new JsonParser();
JsonObject jo = jp.parse(body).getAsJsonObject();

for (Map.Entry entry: jo.entrySet()) {
if(entry.getKey().toString()=="success"){
Result = true;
}
break;
}

}

return Result;

}
}

class Log{

public static void addText(String Text, String FileName) throws IOException {

Date d = new Date();

FileWriter writer = new FileWriter(FileName, true);
writer.write(d.toString() + " " + Text);
writer.append('\n');
writer.flush();
}

}


