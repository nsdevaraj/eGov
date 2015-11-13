/* eGov suite of products aim to improve the internal efficiency,transparency,
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

	1) All versions of this program, verbatim or modified must carry this
	   Legal Notice.

	2) Any misrepresentation of the origin of the material is prohibited. It
	   is required that all modified versions of this material be marked in
	   reasonable ways as different from the original version.

	3) This license does not grant any rights to any user of the program
	   with regards to rights under trademark law for use of the trade names
	   or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.infra.admin.master.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.LocaleUtils;
import org.egov.infra.persistence.entity.AbstractAuditable;
import org.egov.infra.persistence.entity.Address;
import org.egov.infra.persistence.entity.enums.Gender;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.persistence.validator.annotation.Unique;
import org.egov.infra.validation.regex.Constants;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "eg_user")
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@SequenceGenerator(name = User.SEQ_USER, sequenceName = User.SEQ_USER, allocationSize = 1)
@Unique(id = "id", tableName = "eg_user", columnName = { "username", "mobileNumber", "pan", "aadhaarNumber",
        "emailId" }, fields = { "username", "mobileNumber", "pan", "aadhaarNumber", "emailId" }, enableDfltMsg = true)
public class User extends AbstractAuditable {
    private static final long serialVersionUID = -2415368058955783970L;
    public static final String SEQ_USER = "SEQ_EG_USER";

    @Expose
    @Id
    @GeneratedValue(generator = SEQ_USER, strategy = GenerationType.SEQUENCE)
    @DocumentId
    private Long id;

    @NotNull
    @Column(name = "username", unique = true)
    @Length(min = 2, max = 64)
    private String username;

    @NotNull
    @Length(min = 8, max = 64)
    private String password;

    private String salutation;

    @SafeHtml
    @Length(min = 2, max = 64)
    private String guardian;

    @SafeHtml
    @Length(min = 2, max = 64)
    private String guardianRelation;

    @NotNull
    @SafeHtml
    @Length(min = 2, max = 100)
    @Audited
    private String name;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @Pattern(regexp = Constants.MOBILE_NUM)
    @SafeHtml
    @Length(max = 15)
    @Audited
    private String mobileNumber;

    @Email(regexp = Constants.EMAIL)
    @SafeHtml
    @Length(max = 128)
    @Audited
    private String emailId;

    @SafeHtml
    private String altContactNumber;

    @SafeHtml
    @Length(max = 10)
    private String pan;

    @SafeHtml
    @Length(max = 20)
    private String aadhaarNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Address> address = new ArrayList<Address>();

    private boolean active;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "eg_userrole", joinColumns = @JoinColumn(name = "userid") , inverseJoinColumns = @JoinColumn(name = "roleid") )
    private Set<Role> roles = new HashSet<>();

    @Temporal(TemporalType.DATE)
    private Date dob;

    @NotNull
    private Date pwdExpiryDate = new Date();

    @NotNull
    private String locale = "en_IN";

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UserType type;

    private byte [] signature;
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(final Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(final String salutation) {
        this.salutation = salutation;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(final Gender gender) {
        this.gender = gender;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(final String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public String getAltContactNumber() {
        return altContactNumber;
    }

    public void setAltContactNumber(final String altContactNumber) {
        this.altContactNumber = altContactNumber;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(final String pan) {
        this.pan = pan;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(final String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(final List<Address> address) {
        this.address = address;
    }

    public void addAddress(final Address address) {
        address.setUser(this);
        this.address.add(address);
    }

    public void removeAddress(final Address address) {
        getAddress().remove(address);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(final Role role) {
        getRoles().add(role);
    }

    public void removeRole(final Role role) {
        getRoles().remove(role);
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(final Date dob) {
        this.dob = dob;
    }

    public DateTime getPwdExpiryDate() {
        return null == pwdExpiryDate ? null : new DateTime(pwdExpiryDate);
    }

    public void setPwdExpiryDate(final Date pwdExpiryDate) {
        this.pwdExpiryDate = pwdExpiryDate;
    }

    public String getLocale() {
        return locale;
    }

    public Locale locale() {
        return LocaleUtils.toLocale(locale);
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public UserType getType() {
        return type;
    }

    public void setType(final UserType userType) {
        type = userType;
    }

    public String getGuardian() {
        return guardian;
    }

    public void setGuardian(final String guardian) {
        this.guardian = guardian;
    }

    public String getGuardianRelation() {
        return guardianRelation;
    }

    public void setGuardianRelation(final String guardianRelation) {
        this.guardianRelation = guardianRelation;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

}
