package dk.magenta.alfresco.behaviour;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour to move documents placed in a site's root folder to its
 * documentLibrary folder.
 * <p/>
 * The main use case is when people accidentally place documents there when
 * connecting to Alfresco via IMAP, instead of placing them in the site's
 * documentLibrary folder.
 */
public class MoveToDocLibFolderBehaviour implements NodeServicePolicies
        .OnCreateChildAssociationPolicy {
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private SiteService siteService;
    private DictionaryService dictionaryService;

    public void init() {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        if (childAssocRef == null || childAssocRef.getParentRef() == null ||
                childAssocRef.getChildRef() == null) {
            return;
        }

        NodeRef parentNodeRef = childAssocRef.getParentRef();
        NodeRef childNodeRef = childAssocRef.getChildRef();
        if (!dictionaryService.isSubClass(nodeService.getType(childNodeRef),
                ContentModel.TYPE_FOLDER)) {
            SiteInfo site = siteService.getSite(parentNodeRef);
            if (!siteService.hasContainer(site.getShortName(), SiteService
                    .DOCUMENT_LIBRARY)) {
                return;
            }

            NodeRef docLibNodeRef = siteService.getContainer(site
                    .getShortName(), SiteService.DOCUMENT_LIBRARY);
            nodeService.moveNode(childNodeRef, docLibNodeRef, ContentModel
                    .ASSOC_CONTAINS, childAssocRef.getQName());
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
