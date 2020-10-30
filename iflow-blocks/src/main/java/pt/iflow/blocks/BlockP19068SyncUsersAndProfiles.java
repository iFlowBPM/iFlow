package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.transition.ProfilesTO;
import pt.iflow.api.userdata.UserData;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068SyncUsersAndProfiles extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String OUTPUT_ERROR_DOCUMENT = "outputErrorDocument";	

	public BlockP19068SyncUsersAndProfiles(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		hasInteraction = false;
	}

	public Port getEventPort() {
		return null;
	}

	public Port[] getInPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[1];
		retObj[0] = portIn;
		return retObj;
	}

	public Port[] getOutPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[2];
		retObj[0] = portSuccess;
		retObj[1] = portEmpty;
		retObj[2] = portError;
		return retObj;
	}

	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		Documents docBean = BeanFactory.getDocumentsBean();
		UserManager userManager = BeanFactory.getUserManagerBean();
		Properties properties = Setup.readPropertiesFile("P19068.properties");

		String sInputDocumentVar = this.getAttribute(INPUT_DOCUMENT);
		String sOutputErrorDocumentVar = this.getAttribute(OUTPUT_ERROR_DOCUMENT);

		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sOutputErrorDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar);
			Document inputDoc = null;
			InputStream inputDocStream = null;
			String originalNameInputDoc = "";			

			docsVar = procData.getList(sInputDocumentVar);
			inputDoc = docBean.getDocument(userInfo, procData, new Integer(docsVar.getItem(0).getValue().toString()));
			inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			originalNameInputDoc = inputDoc.getFileName();			

			ArrayList<String> errorList = new ArrayList<>();
			List<String> lines = IOUtils.readLines(inputDocStream,"UTF-8");
			for(String line: lines){
				//tratar input
				if(StringUtils.isBlank(line))
					continue;
				String username,name,profileAGP,email;
				
				try{
					String[] tokens = line.split(";");
					username = tokens[1];
					name = tokens[2];
					profileAGP = tokens[6];
					email = tokens[7];
				} catch(Exception e){
					Logger.error(login, this, "after", procData.getSignature() + "error parsing line: " + line, e);
					errorList.add("error parsing line: " + line);
					continue;
				}
				//criar users e perfis onde necessario e depois associa
				UserViewInterface uvi = userManager.findUser(userInfo, username);
				if(StringUtils.isBlank(uvi.getUserId())){					
					userManager.createUser(userInfo, username, "M", userInfo.getUserData().getUnitId(), email, name, null, null, null, null, null, userInfo.getOrganization(), null, "xpto456", null, null, " ");
					uvi = userManager.findUser(userInfo, username);
					
					if(StringUtils.isBlank(uvi.getUserId())){
						Logger.error(login, this, "after", procData.getSignature() + "error creating user: "+ username+ " at line : " + line);
						errorList.add("error creating user: "+ username+ " at line : " + line);
						continue;
					}
				}
				
				String mappedProfile = (String) properties.get("agp." + profileAGP);
				String profiles[] = mappedProfile.split(";");
				for(String profile: profiles){
					ProfilesTO[] profileTOList = userManager.getAllProfiles(userInfo);
					ProfilesTO profileTO = null;
					for(ProfilesTO p: profileTOList)
						if(StringUtils.equalsIgnoreCase(p.getName(), profile))
							profileTO = p;
					if(profileTO == null){
						ProfilesTO newProfile = new ProfilesTO(profile, profile, userInfo.getOrganization());
						Boolean sucess = userManager.createProfile(userInfo, newProfile);
						if(!sucess){
							Logger.error(login, this, "after", procData.getSignature() + "error creating profile: "+ profile+ " at line : " + line);
							errorList.add("error creating profile: "+ profile+ " at line : " + line);
							continue;
						}
					}
					profileTOList = userManager.getAllProfiles(userInfo);
					profileTO = null;
					for(ProfilesTO p: profileTOList)
						if(StringUtils.equalsIgnoreCase(p.getName(), profile))
							profileTO = p;
					
					uvi = userManager.findUser(userInfo, username);
					userManager.addUserProfile(userInfo, uvi.getUserId(), "" + profileTO.getProfileId());
				}
				

//				Boolean sucess = userManager.addUserProfile(userInfo, uvi.getUserId(), "" + profileTO.getProfileId());
//				if(!sucess){
//					Logger.error(login, this, "after", procData.getSignature() + "error associating user/profile: "+ username + " , " + profile+ " at line : " + line);
//					errorList.add("error associating user/profile: "+ username + " , " + profile+ " at line : " + line);
//					continue;
//				}
			}

			// set errors file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Document doc = saveFileAsDocument("E" + originalNameInputDoc , errorList, userInfo, procData);
			if (doc != null)
				procData.getList(sOutputErrorDocumentVar).parseAndAddNewItem(String.valueOf(doc.getDocId()));

		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}
		return outPort;
	}

	private Document saveFileAsDocument(String filename, ArrayList<?> errorList, UserInfoInterface userInfo,
			ProcessData procData) throws Exception {
		if (errorList.isEmpty())
			return null;

		File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
		BufferedWriter tmpOutput = new BufferedWriter(new FileWriter(tmpFile, true));
		for (Object aux : errorList) {
			tmpOutput.write(aux.toString());
			tmpOutput.newLine();
		}
		tmpOutput.close();

		Documents docBean = BeanFactory.getDocumentsBean();
		Document doc = new DocumentDataStream(0, null, null, null, 0, 0, 0);
		doc.setFileName(filename);
		FileInputStream fis = new FileInputStream(tmpFile);
		((DocumentDataStream) doc).setContentStream(fis);
		doc = docBean.addDocument(userInfo, procData, doc);
		tmpFile.delete();
		return doc;
	}
	
	@Override
	public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResult(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

}
