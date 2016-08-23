/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.lcms.transactions.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.egov.commons.EgwStatus;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.lcms.transactions.entity.Appeal;
import org.egov.lcms.transactions.entity.Contempt;
import org.egov.lcms.transactions.entity.JudgmentImpl;
import org.egov.lcms.transactions.repository.JudgmentImplRepository;
import org.egov.lcms.utils.LegalCaseUtil;
import org.egov.lcms.utils.constants.LcmsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class JudgmentImplService {

    private final JudgmentImplRepository judgmentImplRepository;

    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;
    @Autowired
    private LegalCaseService legalCaseService;

    @Autowired
    private LegalCaseUtil legalCaseUtil;

    @Autowired
    public JudgmentImplService(final JudgmentImplRepository judgmentImplRepository) {
        this.judgmentImplRepository = judgmentImplRepository;
    }

    @Transactional
    public JudgmentImpl persist(final JudgmentImpl judgmentImpl) {
        persistAppealOrContempt(judgmentImpl);
        /*
         * if (judgmentImpl.getImplementationFailure() != null &&
         * judgmentImpl.getImplementationFailure().toString().equals("Appeal"))
         * processAndStoreAppealDocuments(judgmentImpl,
         * judgmentImpl.getAppeal().get(0).getAppealDocuments());
         */
        return judgmentImplRepository.save(judgmentImpl);
    }

    @Transactional
    public void saveOrUpdate(final JudgmentImpl judgmentImpl) {
        persist(judgmentImpl);
        final EgwStatus statusObj = legalCaseUtil.getStatusForModuleAndCode(LcmsConstants.MODULE_TYPE_LEGALCASE,
                LcmsConstants.JUDGMENTIMPLEMENT_STATUS);
        judgmentImpl.getJudgment().getLegalCase().setStatus(statusObj);
        legalCaseService.save(judgmentImpl.getJudgment().getLegalCase());

    }

    @Transactional
    public void persistAppealOrContempt(final JudgmentImpl judgmentImpl) {

        if (judgmentImpl.getContempt().get(0).getCaNumber() != null)
            for (final Contempt judgmentImplcontempt : judgmentImpl.getContempt()) {
                judgmentImplcontempt.setJudgmentImpl(judgmentImpl);
                judgmentImplcontempt.getVersion();
                judgmentImpl.getContempt().add(judgmentImplcontempt);
                break;
            }
        final Set<Contempt> contemptSet = new HashSet<Contempt>();
        if (judgmentImpl.getContempt().get(0).getCaNumber() != null)
            contemptSet.addAll(judgmentImpl.getContempt());

        judgmentImpl.getContempt().clear();
        judgmentImpl.getContempt().addAll(contemptSet);

        if (judgmentImpl.getAppeal().get(0).getSrNumber() != null)
            for (final Appeal appealObj : judgmentImpl.getAppeal())
                if (appealObj.getSrNumber() != null && !"".equals(appealObj.getSrNumber())) {
                    appealObj.setJudgmentImpl(judgmentImpl);
                    judgmentImpl.getAppeal().clear();
                    judgmentImpl.getAppeal().add(appealObj);
                     break;
                }
        final Set<Appeal> apealSet = new HashSet<Appeal>();
        if (judgmentImpl.getAppeal().get(0).getSrNumber() != null)
            apealSet.addAll(judgmentImpl.getAppeal());
        judgmentImpl.getAppeal().clear();
        judgmentImpl.getAppeal().addAll(apealSet);

    }

    /*
      public List<AppealDocuments> getAppealDocList(final JudgmentImpl
     * judgmentImpl) { final List<AppealDocuments> judgmentImplAppealDOc = new
     * ArrayList<AppealDocuments>(); final Set<AppealDocuments> appealDOcSet =
     * new HashSet<AppealDocuments>(); if (!judgmentImpl.getAppeal().isEmpty()
     * && judgmentImpl.getAppeal().get(0) != null) { for (final AppealDocuments
     * appealDocs : judgmentImpl.getAppeal().get(0).getAppealDocuments())
     * appealDOcSet.add(appealDocs); judgmentImplAppealDOc.addAll(appealDOcSet);
     * } return judgmentImplAppealDOc; }
     */

    /*
     * public void processAndStoreAppealyyDocuments(final JudgmentImpl
     * judgmentImpl,final List<Appeal> appeal) { if
     * (!judgmentImpl.getAppeal().get(0).getAppealDocuments().isEmpty()) for
     * (final AppealDocuments appeal :
     * judgmentImpl.getAppeal().get(0).getAppealDocuments()) if (appeal != null
     * && appeal.getId() == null) {
     * appeal.setAppeal(judgmentImpl.getAppeal().get(0));
     * appeal.setDocumentName("Appeal");
     * appeal.setSupportDocs(addToFileStore(appeal.getFiles())); } }
     */
   
    /* @Transactional 
     public void processAndStoreAppealDocuments(final JudgmentImpl judgmentImpl, 
             final List<AppealDocuments> appealDoc) { 
         if (judgmentImpl.getAppeal().get(0).getId() == null) { if
      (!judgmentImpl.getAppeal().get(0).getAppealDocuments().isEmpty()) for
      (final AppealDocuments appealDocument : appealDoc) {
     if(appealDocument.getFiles() !=null){
      appealDocument.setAppeal(judgmentImpl.getAppeal().get(0));
      appealDocument.setDocumentName("Appeal");
      appealDocument.setSupportDocs(addToFileStore(appealDocument.getFiles()));
      //appealDocumentsRepository.save(appealDocument); } } 
     }
     
*/
    protected Set<FileStoreMapper> addToFileStore(final MultipartFile[] files) {
        if (ArrayUtils.isNotEmpty(files))
            return Arrays.asList(files).stream().filter(file -> !file.isEmpty()).map(file -> {
                try {
                    return fileStoreService.store(file.getInputStream(), file.getOriginalFilename(),
                            file.getContentType(), LcmsConstants.FILESTORE_MODULECODE);
                } catch (final Exception e) {
                    throw new ApplicationRuntimeException("Error occurred while getting inputstream", e);
                }
            }).collect(Collectors.toSet());
        else
            return null;
    }

    public List<JudgmentImpl> findAll() {
        return judgmentImplRepository.findAll(new Sort(Sort.Direction.ASC, " "));
    }

    public JudgmentImpl findOne(final Long id) {
        return judgmentImplRepository.findOne(id);
    }

}