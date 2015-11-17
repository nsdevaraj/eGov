/* eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 *
 * Copyright (C) <2015> eGovernments Foundation
 *
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 *
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 *
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 *
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 *
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 *
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.api.controller;

import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.egov.api.adapter.ComplaintAdapter;
import org.egov.api.adapter.ComplaintStatusAdapter;
import org.egov.api.adapter.ComplaintTypeAdapter;
import org.egov.api.controller.core.ApiController;
import org.egov.api.controller.core.ApiUrl;
import org.egov.api.model.ComplaintSearchRequest;
import org.egov.config.search.Index;
import org.egov.config.search.IndexType;
import org.egov.infra.admin.master.entity.CrossHierarchy;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.admin.master.service.CrossHierarchyService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.pgr.entity.Complaint;
import org.egov.pgr.entity.ComplaintStatus;
import org.egov.pgr.entity.ComplaintType;
import org.egov.pgr.service.ComplaintService;
import org.egov.pgr.service.ComplaintStatusService;
import org.egov.pgr.service.ComplaintTypeService;
import org.egov.pgr.utils.constants.PGRConstants;
import org.egov.search.domain.SearchResult;
import org.egov.search.domain.Sort;
import org.egov.search.service.SearchService;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/v1.0")
public class ComplaintController extends ApiController {

	private static final Logger LOGGER = Logger.getLogger(ComplaintController.class);
	
    @Autowired
    protected ComplaintStatusService complaintStatusService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private BoundaryService boundaryService;

    @Autowired(required = true)
    protected ComplaintService complaintService;

    @Autowired
    protected ComplaintTypeService complaintTypeService;

    @Autowired
    protected CrossHierarchyService crossHierarchyService;

    // --------------------------------------------------------------------------------//
    /**
     * This will returns all complaint types
     *
     * @return ComplaintType
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_GET_TYPES }, method = GET, produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> getAllTypes() {
        try {
            final List<ComplaintType> complaintTypes = complaintTypeService.findActiveComplaintTypes();
            return getResponseHandler().setDataAdapter(new ComplaintTypeAdapter()).success(complaintTypes);
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ",e);
        	return getResponseHandler().error(getMessage("server.error"));
        }
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will returns complaint types which is frequently filed.
     *
     * @return ComplaintType
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_GET_FREQUENTLY_FILED_TYPES }, method = GET, produces = {
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> getFrequentTypes() {
        try {
            final List<ComplaintType> complaintTypes = complaintTypeService.getFrequentlyFiledComplaints();
            return getResponseHandler().setDataAdapter(new ComplaintTypeAdapter()).success(complaintTypes);
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ",e);
        	return getResponseHandler().error(getMessage("server.error"));
        }
    }

    // --------------------------------------------------------------------------------//
    /**
     * Searching complaint using any text.
     *
     * @param searchRequest
     * @return
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_SEARCH }, method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> complaintSearch(@RequestBody final ComplaintSearchRequest searchRequest) {
       try
       {
    	final SearchResult searchResult = searchService.search(
                asList(Index.PGR.toString()),
                asList(IndexType.COMPLAINT.toString()),
                searchRequest.searchQuery(), searchRequest.searchFilters(),
                Sort.by().field("common.createdDate", SortOrder.DESC), org.egov.search.domain.Page.NULL);
        return getResponseHandler().success(searchResult.getDocuments());
       }
       catch(Exception e)
       {
    	 LOGGER.error("EGOV-API ERROR ",e);
       	 return getResponseHandler().error(getMessage("server.error"));
       }
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will create a complaint
     *
     * @param Complaint - As json Object
     * @return Complaint
     */
    /*@RequestMapping(value = ApiUrl.COMPLAINT_CREATE, method = RequestMethod.POST)
    public ResponseEntity<String> complaintCreate(@RequestBody final JSONObject complaintRequest) {

        try {
            final Complaint complaint = new Complaint();
            final long complaintTypeId = (int) complaintRequest.get("complaintTypeId");
            if (complaintRequest.get("locationId") != null && (int) complaintRequest.get("locationId") > 0) {
                final long locationId = (int) complaintRequest.get("locationId");
                final CrossHierarchy crosshierarchy = crossHierarchyService.findById(locationId);
                complaint.setLocation(crosshierarchy.getParent());
                complaint.setChildLocation(crosshierarchy.getChild());
            }
            if (complaintRequest.get("lng") != null && (double) complaintRequest.get("lng") > 0) {
                final double lng = (double) complaintRequest.get("lng");
                complaint.setLng(lng);
            }
            if (complaintRequest.get("lat") != null && (double) complaintRequest.get("lat") > 0) {
                final double lat = (double) complaintRequest.get("lat");
                complaint.setLat(lat);
            }
            if (complaint.getLocation() == null && (complaint.getLat() == 0 || complaint.getLng() == 0))
                return getResponseHandler().error(getMessage("location.required"));
            complaint.setDetails(complaintRequest.get("details").toString());
            complaint.setLandmarkDetails(complaintRequest.get("landmarkDetails").toString());
            if (complaintTypeId > 0) {
                final ComplaintType complaintType = complaintTypeService.findBy(complaintTypeId);
                complaint.setComplaintType(complaintType);
            }
            complaintService.createComplaint(complaint);
            return getResponseHandler().setDataAdapter(new ComplaintAdapter()).success(complaint,
                    getMessage("msg.complaint.reg.success"));
        } catch (final Exception e) {
            return getResponseHandler().error(e.getMessage());
        }
    }*/
    
    
    @RequestMapping(value = ApiUrl.COMPLAINT_CREATE, method = RequestMethod.POST)
    public ResponseEntity<String> complaintCreate(@RequestParam(value = "json_complaint", required = false) String complaintJSON, @RequestParam("files") final MultipartFile[] files) {
    	try {
    		
    		JSONObject complaintRequest=(JSONObject)JSONValue.parse(complaintJSON);
    		
            final Complaint complaint = new Complaint();
            final long complaintTypeId = (long) complaintRequest.get("complaintTypeId");
            if (complaintRequest.get("locationId") != null && (long) complaintRequest.get("locationId") > 0) {
                final long locationId = (long) complaintRequest.get("locationId");
                final CrossHierarchy crosshierarchy = crossHierarchyService.findById(locationId);
                complaint.setLocation(crosshierarchy.getParent());
                complaint.setChildLocation(crosshierarchy.getChild());
            }
            if (complaintRequest.get("lng") != null && (double) complaintRequest.get("lng") > 0) {
                final double lng = (double) complaintRequest.get("lng");
                complaint.setLng(lng);
            }
            if (complaintRequest.get("lat") != null && (double) complaintRequest.get("lat") > 0) {
                final double lat = (double) complaintRequest.get("lat");
                complaint.setLat(lat);
            }
            if (complaint.getLocation() == null && (complaint.getLat() == 0 || complaint.getLng() == 0))
                return getResponseHandler().error(getMessage("location.required"));
            complaint.setDetails(complaintRequest.get("details").toString());
            complaint.setLandmarkDetails(complaintRequest.get("landmarkDetails").toString());
            if (complaintTypeId > 0) {
                final ComplaintType complaintType = complaintTypeService.findBy(complaintTypeId);
                complaint.setComplaintType(complaintType);
            }
            if(files.length>0)
            {
            	complaint.setSupportDocs(addToFileStore(files));	
            }
            complaintService.createComplaint(complaint);
            return getResponseHandler().setDataAdapter(new ComplaintAdapter()).success(complaint,
                   getMessage("msg.complaint.reg.success"));
        } catch (ValidationException e) {
        	return getResponseHandler().error(getMessage(e.getMessage()));
        } catch (Exception e) {
			// TODO: handle exception
        	LOGGER.error("EGOV-API ERROR ",e);
          	return getResponseHandler().error(getMessage("server.error"));
		}
    }
    
    protected Set<FileStoreMapper> addToFileStore(final MultipartFile[] files) {
        if (ArrayUtils.isNotEmpty(files))
            return Arrays.asList(files).stream().filter(file -> !file.isEmpty()).map(file -> {
                try {
                    return fileStoreService.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(),
                            PGRConstants.MODULE_NAME);
                } catch (final Exception e) {
                    throw new ApplicationRuntimeException("err.input.stream", e);
                }
            }).collect(Collectors.toSet());
        else
            return null;
    }
    

    // --------------------------------------------------------------------------------//
    /**
     * This will upload complaint support document
     *
     * @param complaintNo
     * @param files
     * @return
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_UPLOAD_SUPPORT_DOCUMENT }, method = RequestMethod.POST)
    public ResponseEntity<String> uploadSupportDocs(@PathVariable final String complaintNo,
            @RequestParam("files") final MultipartFile file) {
    	try {
            final Complaint complaint = complaintService.getComplaintByCRN(complaintNo);

            final FileStoreMapper uploadFile = fileStoreService.store(
                    file.getInputStream(), file.getOriginalFilename(),
                    file.getContentType(), PGRConstants.MODULE_NAME);
            complaint.getSupportDocs().add(uploadFile);
            complaintService.update(complaint, null, null);
            return getResponseHandler().success("", getMessage("msg.complaint.update.success"));
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ",e);
          	return getResponseHandler().error(getMessage("server.error"));
        }
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will display the detail of the complaint
     *
     * @param complaintNo
     * @return Complaint
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_DETAIL }, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getDetail(@PathVariable final String complaintNo) {
    	try
    	{
	        if (complaintNo == null)
	            return getResponseHandler().error("Invalid number");
	        final Complaint complaint = complaintService.getComplaintByCRN(complaintNo);
	        if (complaint == null)
	            return getResponseHandler().error("no complaint information");
	        return getResponseHandler().setDataAdapter(new ComplaintAdapter()).success(complaint);
    	}
    	catch(Exception e)
    	{
    		LOGGER.error("EGOV-API ERROR ",e);
          	return getResponseHandler().error(getMessage("server.error"));
    	}
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will display the status history of the complaint( status : REGISTERED, FORWARDED..).
     *
     * @param complaintNo
     * @return Complaint
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_STATUS }, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getStatus(@PathVariable final String complaintNo) {
		try {
			if (complaintNo == null)
				return getResponseHandler().error("Invalid number");
			final Complaint complaint = complaintService
					.getComplaintByCRN(complaintNo);
			if (complaint == null)
				return getResponseHandler().error("no complaint information");
			else
				return getResponseHandler().setDataAdapter(
						new ComplaintStatusAdapter()).success(
						complaint.getStateHistory());
		} catch (Exception e) {
			LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
		}
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will display the latest complaint except current user.
     *
     * @param page
     * @param pageSize
     * @return Complaint
     */

    @RequestMapping(value = { ApiUrl.COMPLAINT_LATEST }, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLatest(@PathVariable("page") final int page, @PathVariable("pageSize") final int pageSize) {

        if (page < 1)
            return getResponseHandler().error("Invalid Page Number");
        try {
            final Page<Complaint> pagelist = complaintService.getLatest(page, pageSize);
            final boolean hasNextPage = pagelist.getTotalElements() > page * pageSize;
            return getResponseHandler().putStatusAttribute("hasNextPage", String.valueOf(hasNextPage))
                    .setDataAdapter(new ComplaintAdapter()).success(pagelist.getContent());
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }

    }

    // --------------------------------------------------------------------------------//

    /**
     * This will returns the location(name, address) based on given characters.
     *
     * @param locationName
     * @return
     */
    @RequestMapping(value = { ApiUrl.COMPLAINT_GET_LOCATION }, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLocation(@RequestParam("locationName") final String locationName) {
    	try
    	{
	        if (locationName == null || locationName.isEmpty() || locationName.length() < 3)
	            return getResponseHandler().error(getMessage("location.search.invalid"));
	        final List<Map<String, Object>> list = boundaryService.getBoundaryDataByNameLike(locationName);
	        return getResponseHandler().success(list);
    	} catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }
    }

    // --------------------------------------------------------------------------------//
    /**
     * This will returns complaint list of current user.
     *
     * @param page
     * @param pageSize
     * @return Complaint
     */

    @RequestMapping(value = {
            ApiUrl.CITIZEN_GET_MY_COMPLAINT }, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMyComplaint(@PathVariable("page") final int page,
            @PathVariable("pageSize") final int pageSize) {

        if (page < 1)
            return getResponseHandler().error("Invalid Page Number");
        try {
            final Page<Complaint> pagelist = complaintService.getMyComplaint(page, pageSize);
            final boolean hasNextPage = pagelist.getTotalElements() > page * pageSize;
            return getResponseHandler().putStatusAttribute("hasNextPage", String.valueOf(hasNextPage))
                    .setDataAdapter(new ComplaintAdapter()).success(pagelist.getContent());
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }

    }

    // --------------------------------------------------------------------------------//

    /**
     * This will returns nearest user complaint list.
     *
     * @param page
     * @param pageSize
     * @param lat
     * @param lng
     * @param distance
     * @return Complaint
     */
    @RequestMapping(value = ApiUrl.COMPLAINT_NEARBY, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getNearByComplaint(@PathVariable("page") final int page, @RequestParam("lat") final float lat,
            @RequestParam("lng") final float lng, @RequestParam("distance") final int distance,
            @PathVariable("pageSize") final int pageSize) {

        if (page < 1)
            return getResponseHandler().error("Invalid Page Number");
        try {
            final List<Complaint> list = complaintService.getNearByComplaint(page, lat, lng, distance, pageSize);
            boolean hasNextPage = false;
            if (list.size() > pageSize) {
                hasNextPage = true;
                list.remove(pageSize);
            }
            return getResponseHandler().putStatusAttribute("hasNextPage", String.valueOf(hasNextPage))
                    .setDataAdapter(new ComplaintAdapter()).success(list);
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }
    }

    // ------------------------------------------

    /**
     * This will download the support document of the complaint.
     *
     * @param complaintNo
     * @param fileNo
     * @param isThumbnail
     * @return file
     */

    @RequestMapping(value = ApiUrl.COMPLAINT_DOWNLOAD_SUPPORT_DOCUMENT, method = RequestMethod.GET)
    public void getComplaintDoc(@PathVariable final String complaintNo,
            @RequestParam(value = "fileNo", required = false) Long fileNo,
            @RequestParam(value = "isThumbnail", required = false, defaultValue = "false") final boolean isThumbnail,
            final HttpServletResponse response) throws IOException {
        try {
            final Complaint complaint = complaintService.getComplaintByCRN(complaintNo);
            final Set<FileStoreMapper> files = complaint.getSupportDocs();
            int i = 1;
            final Iterator<FileStoreMapper> it = files.iterator();
            if (fileNo == null)
                fileNo = (long) files.size();
            File downloadFile = null;
            while (it.hasNext()) {
                final FileStoreMapper fm = it.next();
                if (i == fileNo) {
                    downloadFile = fileStoreService.fetch(fm.getFileStoreId(), PGRConstants.MODULE_NAME);
                    final ByteArrayOutputStream thumbImg = new ByteArrayOutputStream();
                    long contentLength = downloadFile.length();

                    if (isThumbnail) {
                        final BufferedImage img = Thumbnails.of(downloadFile).size(200, 200).asBufferedImage();
                        ImageIO.write(img, "jpg", thumbImg);
                        thumbImg.close();
                        contentLength = thumbImg.size();
                    }

                    response.setHeader("Content-Length", String.valueOf(contentLength));
                    response.setHeader("Content-Disposition", "attachment;filename=" + fm.getFileName());
                    response.setContentType(Files.probeContentType(downloadFile.toPath()));
                    final OutputStream out = response.getOutputStream();
                    IOUtils.write(isThumbnail == true ? thumbImg.toByteArray() : FileUtils.readFileToByteArray(downloadFile),
                            out);
                    IOUtils.closeQuietly(out);
                    break;
                }
                i++;
            }
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
            throw new IOException();
        }
    }

    // ---------------------------------------------------------------------//
    /**
     * This will update the status of the complaint.
     *
     * @param complaintNo
     * @param As a json object ( action, comment)
     * @return Complaint
     */

    @RequestMapping(value = ApiUrl.COMPLAINT_UPDATE_STATUS, method = RequestMethod.PUT, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateComplaintStatus(
            @PathVariable final String complaintNo, @RequestBody final JSONObject jsonData) {

        String msg = null;
        try {
            final Complaint complaint = complaintService.getComplaintByCRN(complaintNo);
            final ComplaintStatus cmpStatus = complaintStatusService.getByName(jsonData.get("action").toString());
            complaint.setStatus(cmpStatus);
            complaintService.update(complaint, Long.valueOf(0), jsonData.get("comment").toString());
            return getResponseHandler().success("", getMessage("msg.complaint.status.update.success"));
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }

    }

    @RequestMapping(value = ApiUrl.COMPLAINT_HISTORY, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getComplaintHistory(@PathVariable final String complaintNo) {
        try {
            final HashMap<String, Object> container = new HashMap<>();
            final Complaint complaint = complaintService.getComplaintByCRN(complaintNo);
            final List<Hashtable<String, Object>> list = complaintService.getHistory(complaint);
            container.put("comments", list);
            return getResponseHandler().setDataAdapter(new ComplaintTypeAdapter()).success(container);
        } catch (final Exception e) {
        	LOGGER.error("EGOV-API ERROR ", e);
			return getResponseHandler().error(getMessage("server.error"));
        }
    }

}