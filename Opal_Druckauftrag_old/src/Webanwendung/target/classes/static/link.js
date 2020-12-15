function link(){
    //open download link in new page
    window.open("/download");
    //redirect current page to success page
    window.location=("/download/confirm");
    window.focus();
}