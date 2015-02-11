<%@ page contentType="text/html" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html>
<html lang="en">
<head>
<!-- <meta charset="utf-8"> -->
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<meta name="description" content="Neon Admin Panel" />
<meta name="author" content="" />

<title>eGov Urban Portal</title>

<link rel="stylesheet"
	href="<c:url value='/resources/global/css/bootstrap/bootstrap.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/global/css/font-icons/entypo/css/entypo.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/global/css/egov/custom.css'/>">
<script src="<c:url value='/resources/global/js/jquery/jquery.js'/>"></script>

<!--[if lt IE 9]><script src="assets/js/ie8-responsive-file-warning.js"></script><![endif]-->

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
		<![endif]-->


</head>
<body class="page-body">
	<div class="page-container horizontal-menu">
		<form:form role="form" class="form-horizontal form-groups-bordered"
			modelAttribute="complaint">
			<div class="main-content">
				<div class="row">
					<div class="col-md-12">

						<div class="panel panel-primary" data-collapsed="0">
							<div class="panel-heading">
								<div class="panel-title">
									<strong><spring:message code="lbl.hdr.complaintInfo"/></strong>
								</div>

							</div>
							<div class="panel-body">
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.ctn"/></div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-ctnumber">
										<c:out value="${complaint.CRN}"></c:out>
									</div>
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.complaintDate"/>
									</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-date">2014-12-18 
									</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.compalintDepartment"/></div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-dept">
										<c:out value="${complaint.complaintType.department.deptName}"></c:out>
									</div>
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.filedVia"/></div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-filedvia">
										Internet</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.complaintType"/>
									</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-type">
										<c:out value="${complaint.complaintType.name}"></c:out>
									</div>
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.priority"/></div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-priority">
										Waterlogged road</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.expiration"/></div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-exp">
										In
										<c:out value="${complaint.complaintType.daysToResolve}"></c:out>
										days
									</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.compTitle"/>
									</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-title">
										none</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin"><spring:message code="lbl.compDetails"/></div>
									<div class="col-md-20 col-xs-6 add-margin" id="ct-details">
										<c:out value="${complaint.details}"></c:out>
									</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-12 add-margin"><spring:message code="lbl.compDetails"/>
									</div>
									<div
										class="col-md-9 col-xs-12 add-margin down-file text-center"
										id="links">
										<a href="../resources/images/burjkhalifa.jpg" data-gallery>
											<img src="../resources/images/burjkhalifa.jpg"
											class="img-width add-margin">
										</a> <a href="../resources/images/b.jpg" data-gallery> <img
											src="../resources/images/b.jpg" class="img-width add-margin">
										</a> <a href="../resources/images/c.jpg" data-gallery> <img
											src="../resources/images/c.jpg" class="img-width add-margin">
										</a>
									</div>
								</div>
								<div class="row add-border">

									<div class="col-md-3 col-xs-6 add-margin">Location</div>
									<div class="col-md-9 col-xs-6 add-margin">
										<span class="map-tool-class" data-toggle="tooltip"
											data-placement="top" title=""
											data-original-title="Locate on map"
											onclick="jQuery('#complaint-locate').modal('show', {backdrop: 'static'});"><i
											class="entypo-globe"></i></span> <span id="address_locate"><c:out
												value="${complaint.location.bndryNameLocal}"></c:out></span>
									</div>
								</div>
								<div class="row">
									<div class="col-md-3 col-xs-6 add-margin">Landmark</div>
									<div class="col-md-9 col-xs-6 add-margin" id="ct-lanmark">
										<c:out value="${complaint.landmarkDetails}"></c:out>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>


				<div class="row">
					<div class="col-md-12">

						<div class="panel panel-primary" data-collapsed="0">

							<div class="panel-heading">
								<div class="panel-title">
									<strong>Complainant's Information</strong>
								</div>

							</div>

							<div class="panel-body">

								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin">Name</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-name">
										<c:out value="${complaint.complainant.name}"></c:out>

									</div>
									<div class="col-md-3 col-xs-6 add-margin">Phone Number</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-mobno">
										<c:out value="${complaint.complainant.mobile}"></c:out>
									</div>
								</div>
								<div class="row add-border">
									<div class="col-md-3 col-xs-6 add-margin">Email</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-email">
										<c:out value="${complaint.complainant.email}"></c:out>
									</div>
									<div class="col-md-3 col-xs-6 add-margin">Area</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-area">
										Ashok Nagar</div>
								</div>
								<div class="row">
									<div class="col-md-3 col-xs-6 add-margin">City</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-city">
										Bangalore</div>
									<div class="col-md-3 col-xs-6 add-margin">Pincode</div>
									<div class="col-md-3 col-xs-6 add-margin" id="ct-pincode">
										641028</div>
								</div>
							</div>

						</div>

					</div>
				</div>


				<div class="row">
					<div class="col-md-12">
						<div class="panel-group joined">

							<div class="panel panel-primary">
								<div class="panel-heading">
									<h4 class="panel-title">
										<a data-toggle="collapse" href="#collapseOne-2"> <strong>Complaint
												History</strong>
										</a>
									</h4>
								</div>
								<div id="collapseOne-2" class="panel-collapse collapse in">
									<div class="panel-body">
										<div class="row add-border">
											<div class="col-md-3 col-xs-6 add-margin">10:14 AM
												Monday 22 Nov 2014</div>
											<div class="col-md-3 col-xs-6 add-margin">Suresh.T.M</div>
											<div class="col-md-3 col-xs-6 add-margin">Horticulture
											</div>
											<div class="col-md-3 col-xs-6 add-margin">I have
												changed the status of the complaint. Kindly review it. For
												Further information, you can contact me.</div>
										</div>
										<div class="row add-border">
											<div class="col-md-3 col-xs-6 add-margin">10:14 AM
												Monday 22 Nov 2014</div>
											<div class="col-md-3 col-xs-6 add-margin">Suresh.T.M</div>
											<div class="col-md-3 col-xs-6 add-margin">Horticulture
											</div>
											<div class="col-md-3 col-xs-6 add-margin">I have
												changed the status of the complaint. Kindly review it. For
												Further information, you can contact me.</div>
										</div>

										<div class="row">
											<div class="col-md-3 col-xs-6 add-margin">10:14 AM
												Monday 22 Nov 2014</div>
											<div class="col-md-3 col-xs-6 add-margin">Suresh.T.M</div>
											<div class="col-md-3 col-xs-6 add-margin">Horticulture
											</div>
											<div class="col-md-3 col-xs-6 add-margin">I have
												changed the status of the complaint. Kindly review it. For
												Further information, you can contact me.</div>
										</div>
									</div>
								</div>
							</div>


						</div>

					</div>
				</div>
			</div>
		</form:form>
	</div>


	<!-- Modal 6 (Long Modal)-->
	<div class="modal fade" id="complaint-locate">
		<div class="modal-dialog">
			<div class="modal-content">

				<div class="modal-body">

					<div class="row">

						<div class="col-md-12">
							<div class="panel panel-primary" data-collapsed="0">
								<!-- to apply shadow add class "panel-shadow" -->
								<!-- panel head -->
								<div class="panel-heading">
									<div class="panel-title" id="show_address_in_map">Complaint
										Location</div>

								</div>

								<!-- panel body -->
								<div class="panel-body no-padding">

									<script src="https://maps.googleapis.com/maps/api/js"></script>

									<div id="normal" class="img-prop"></div>

								</div>

							</div>
						</div>

					</div>


				</div>

				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				</div>
			</div>
		</div>
	</div>

	<!-- The Bootstrap Image Gallery lightbox, should be a child element of the document body -->
	<div id="blueimp-gallery" class="blueimp-gallery"
		data-use-bootstrap-modal="false">
		<!-- The container for the modal slides -->
		<div class="slides"></div>
		<!-- Controls for the borderless lightbox -->
		<h3 class="title"></h3>
		<ol class="indicator"></ol>
		<!-- The modal dialog, which will be used to wrap the lightbox content -->
		<div class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" aria-hidden="true">&times;</button>
						<h4 class="modal-title"></h4>
					</div>
					<div class="modal-body next"></div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default pull-left prev">
							<i class="glyphicon glyphicon-chevron-left"></i> Previous
						</button>
						<button type="button" class="btn btn-primary next">
							Next <i class="glyphicon glyphicon-chevron-right"></i>
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<script
		src="<c:url value='/resources/global/js/bootstrap/bootstrap.js'/>"></script>

	<link rel="stylesheet"
		href="<c:url value='/resources/global/js/image-gallery/css/blueimp-gallery.min.css'/>">
	<link rel="stylesheet"
		href="<c:url value='/resources/global/js/image-gallery/css/blueimp-gallery.min.css'/>">

	<script
		src="<c:url value='/resources/global/js/image-gallery/js/jquery.blueimp-gallery.min.js'/>"></script>
	<script
		src="<c:url value='/resources/global/js/image-gallery/js/bootstrap-image-gallery.js'/>"></script>

	<script src="<c:url value='/resources/js/app/complaintview.js'/>"></script>
	<script src="<c:url value='/resources/js/app/fileuploadndmaps.js'/>"></script>


</body>
</html>
