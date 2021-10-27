package pt.iflow.blocks;


import org.apache.commons.lang.StringUtils;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.RepositoryFile;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;


public class BlockDocxTemplateGetFileList extends Block {
    public Port portIn, portSuccess, portEmpty, portError;

    private static final String outputTemplate = "outputTemplate";
    private static final String DOCTYPE = ".docx";


    public BlockDocxTemplateGetFileList(int anFlowId, int id, int subflowblockid, String subflow) {
        super(anFlowId, id, subflowblockid, subflow);
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
        Port[] retObj = new Port[3];
        retObj[0] = portSuccess;
        retObj[1] = portEmpty;
        retObj[2] = portError;
        return retObj;
    }

    /**
     * No action in this block
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return always 'true'
     */
    public String before(UserInfoInterface userInfo, ProcessData procData) {
        return "";
    }

    /**
     * No action in this block
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return always 'true'
     */
    public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
        return true;
    }

    /**
     * Executes the block main action
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return the port to go to the next block
     */
    public Port after(UserInfoInterface userInfo, ProcessData procData) {
        Port outPort = portSuccess;
        String login = userInfo.getUtilizador();
        StringBuffer logMsg = new StringBuffer();


        ProcessListVariable outputFileNameListVar = null;


        try {

            // needs array in order for user to chose correct file template
            outputFileNameListVar = procData.getList(this.getAttribute(outputTemplate));


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (outputFileNameListVar == null) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {
                int auxiliary = 0;

                //find all templates
                RepositoryFile[] FileList = BeanFactory.getRepBean().listPrintTemplates(userInfo);

                //check if any found
                if (FileList.length <= 0) {

                    Logger.error(login, "BlockDocxTemplateGetFileList", "after",
                            "No files were found");
                    outPort = portEmpty;
                } else {

                    //filter list for files ending with .docx
                    for (RepositoryFile repFile : FileList) {
                        if (repFile.exists() && StringUtils.endsWithIgnoreCase(repFile.getName(), DOCTYPE)) {
                            auxiliary++;
                            outputFileNameListVar.parseAndAddNewItem("" + repFile.getName());
                        }
                    }

                    Logger.info(login, "BlockDocxTemplateGetFileList", "after",
                            "Files processed correctly");

                    outPort = portSuccess;

                    if (auxiliary == 0) {
                        Logger.error(login, "BlockDocxTemplateGetFileList", "after",
                                "No files were found");
                        outPort = portEmpty;
                    }
                }

            } catch (Exception e) {
                Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
                outPort = portError;
            }

        logMsg.append("Using '" + outPort.getName() + "';");
        Logger.logFlowState(userInfo, procData, this, logMsg.toString());
        return outPort;
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
