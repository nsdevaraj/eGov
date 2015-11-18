/*#-------------------------------------------------------------------------------
	# eGov suite of products aim to improve the internal efficiency,transparency, 
	#    accountability and the service delivery of the government  organizations.
	# 
	#     Copyright (C) <2015>  eGovernments Foundation
	# 
	#     The updated version of eGov suite of products as by eGovernments Foundation 
	#     is available at http://www.egovernments.org
	# 
	#     This program is free software: you can redistribute it and/or modify
	#     it under the terms of the GNU General Public License as published by
	#     the Free Software Foundation, either version 3 of the License, or
	#     any later version.
	# 
	#     This program is distributed in the hope that it will be useful,
	#     but WITHOUT ANY WARRANTY; without even the implied warranty of
	#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	#     GNU General Public License for more details.
	# 
	#     You should have received a copy of the GNU General Public License
	#     along with this program. If not, see http://www.gnu.org/licenses/ or 
	#     http://www.gnu.org/licenses/gpl.html .
	# 
	#     In addition to the terms of the GPL license to be adhered to in using this
	#     program, the following additional terms are to be complied with:
	# 
	# 	1) All versions of this program, verbatim or modified must carry this 
	# 	   Legal Notice.
	# 
	# 	2) Any misrepresentation of the origin of the material is prohibited. It 
	# 	   is required that all modified versions of this material be marked in 
	# 	   reasonable ways as different from the original version.
	# 
	# 	3) This license does not grant any rights to any user of the program 
	# 	   with regards to rights under trademark law for use of the trade names 
	# 	   or trademarks of eGovernments Foundation.
	# 
	#   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
#-------------------------------------------------------------------------------*/

jQuery('#selectAll').click(function(e){
    var table= jQuery(e.target).closest('table');
    jQuery('td input:checkbox',table).prop('checked',e.target.checked);  
});

function callPreview(obj){
	var rowobj=getRow(obj);
	var tbl = document.getElementById('digSignDetailsTab');
	var basicPropertyId=getControlInBranch(tbl.rows[rowobj.rowIndex],'objectId').value;
	var actionName = 'Preview';
	var params = [
		   			'height='+screen.height, 
		   		    'width='+screen.width,
		   		    'fullscreen=yes' 
		   		].join(',');
		var noticeType='${noticeType}';  
			   	window.open("../notice/propertyTaxNotice-generateNotice.action?basicPropId="+basicPropertyId+"&noticeType="+noticeType+"noticeMode=create&actionType="+actionName, "NoticeWindow", params);
			   	return false; 
}

function generateNotice(obj,actionName){
	var rowobj=getRow(obj);
	var tbl = document.getElementById('digSignDetailsTab');
	var basicPropertyId=getControlInBranch(tbl.rows[rowobj.rowIndex],'objectId').value;
		var noticeType = '${noticeType}';  

	jQuery.ajax({
		url: "/ptis/notice/propertyTaxNotice-generateNotice.action?basicPropId="+basicPropertyId+"&noticeType="+noticeType+"&noticeMode=create&actionType="+actionName,
		type: "GET",
		dataType: "json",
		success: function (response) {  
			console.log("completed"+response);
			alert(response); 
		}, 
		error: function (response) {
			console.log("failed");
		}
	});
}

jQuery('#submitButton').click(function(e){
	 if(jQuery('#digSignDetailsTab').find('input[type=checkbox]:checked').length == 0) 
	    {
	        alert('Please select atleast one checkbox');
	        return false;
	    }
	 else {
			var tbl=document.getElementById("digSignDetailsTab");
		    var lastRow = (tbl.rows.length)-1;
		    var idArray = new Array() ; 
		    var j=0;
		    for(var i=1;i<=lastRow;i++){
			    if(getControlInBranch(tbl.rows[i],'rowCheckBox').checked){
			    	idArray[j++]=getControlInBranch(tbl.rows[i],'objectId').value;
				} 
		    } 
		    jQuery.ajax({
				url: "/ptis/notice/propertyTaxNotice-generateBulkNotice.action?basicPropertyIds="+idArray.toString(),
				type: "GET",
				dataType: "json",
				success: function (response) {
					alert("Selected Records Signed Successfully.");
				}, 
				error: function (response) {
					alert("Unable to Sign Selected Records.");
					console.log("failed");
				}
			});
	 } 
});
				







