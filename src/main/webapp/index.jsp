<%@ page language="java"  contentType="text/html; charset=UTF-8" %>
<html>
<body>
<h2>Hello World!</h2>
<h1>tomcat1</h1>
<h1>tomcat1</h1>
<h1>tomcat1</h1>


<form name="form11" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="springmvcuploadfile"/>
</form>

<form name="form12" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="richtextUploadfile"/>
</form>


</body>
</html>
