package com.dell.asm.asmcore.asmmanager.db.entity;


import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Table to represent an OS repository created on ASM system
 */

@Entity
@Table(name="os_repository", schema="public")
public class OSRepositoryEntity extends BaseEntityAudit {

    private static final long serialVersionUID = 1L;

    @Column(name="image_type")
    private String imageType;

    @Column(name="source_path")
    private String sourcePath;

    @Column(name="state")
    private String state;

    @Column(name="razor_name")
    private String razorName;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getRazorName() {
        return razorName;
    }

    public void setRazorName(String razorName) {
        this.razorName = razorName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String decryptedPassword() {
        // Decrypt password
        String password;
        if (StringUtils.isEmpty(this.getPassword())) {
            password = "";
        } else {
            EncryptionDAO encryptionDAO = EncryptionDAO.getInstance();
            IEncryptedString encryptedString = encryptionDAO.findEncryptedStringById(this.getPassword());
            password = encryptedString.getString();
        }
        return password;
    }

    public OSRepository toOSRepository(){
        OSRepository repo = new OSRepository();
        repo.setCreatedBy(this.getCreatedBy());
        repo.setCreatedDate(this.getCreatedDate());
        repo.setId(this.getId());
        repo.setImageType(this.getImageType());
        repo.setName(this.getName());
        repo.setSourcePath(this.getSourcePath());
        repo.setState(this.getState());
        repo.setRazorName(this.getRazorName());
        repo.setUsername(this.getUsername());

        String password;
        if (StringUtils.isEmpty(this.getPassword())) {
            password = "";
        } else {
            password = null;
        }
        repo.setPassword(password);
        return repo;
    }

    public OSRepositoryEntity(){ super(); }

    public OSRepositoryEntity(OSRepository osRepo)
    {
        super();
        this.setId(osRepo.getId());
        this.imageType = osRepo.getImageType();
        this.state = OSRepository.STATE_COPYING;
        this.sourcePath = osRepo.getSourcePath();
        this.setCreatedBy(osRepo.getCreatedBy());
        this.setCreatedDate(osRepo.getCreatedDate());
        this.setName(osRepo.getName());
        this.setRazorName(osRepo.getRazorName());
        this.setUsername(osRepo.getUsername());

        /**
         * Encrypt the password
         */
        String plaintext = osRepo.getPassword();
        this.encryptPassword(plaintext);
    }

    /**
     * Utility method to update only selected fields and optionally save the entity.
     * The reason for the combined operation is to simplify deleting the old encrypted
     * password in the case that the password is being changed.
     * @param osRepo
     */
    public void update(OSRepository osRepo) {
        this.update(osRepo,false);
    }
    public void update(OSRepository osRepo, boolean save)
    {
        this.sourcePath = osRepo.getSourcePath();
        this.setUsername(osRepo.getUsername());

        // Encrypt the new password
        String plaintext = osRepo.getPassword();
        String currentPassword = null;
        if (plaintext == null) {
            // Leave the current password alone
        } else {
            // remember the current password
            currentPassword = this.getPassword();

            // Update this with the new password, encrypted
            this.encryptPassword(plaintext);
        }

        if (save) {
            GenericDAO genericDAO = GenericDAO.getInstance();
            genericDAO.update(this);

            /**
             * Release the current encrypted password string
             */
            if (!StringUtils.isEmpty(currentPassword)) {
                EncryptionDAO encryptionDAO = EncryptionDAO.getInstance();
                IEncryptedString encryptedString = encryptionDAO.findEncryptedStringById(currentPassword);
                if (encryptedString != null) {
                    encryptionDAO.delete(encryptedString);
                }
            }
        }
    }


    private void encryptPassword(String plaintext) {
        /**
         * Update with the new password
         */
        if (StringUtils.isEmpty(plaintext)) {
            this.setPassword("");
        } else {
            // Replace the plaintext value with an encryption id
            EncryptionDAO encryptionDAO = EncryptionDAO.getInstance();
            IEncryptedString encryptedString = encryptionDAO.encryptAndSave(plaintext);
            this.setPassword(encryptedString.getId());
        }
    }
}
