console.log("running module: ShareToPersonDlg.js");

namespace m64 {
    export class ShareToPersonDlg extends DialogBase {

        constructor() {
            super("ShareToPersonDlg");
        }

        /*
         * Returns a string that is the HTML content of the dialog
         */
        build = (): string => {
            var header = this.makeHeader("Share Node to Person");

            var formControls = this.makeEditField("User to Share With", "shareToUserName");
            var shareButton = this.makeCloseButton("Share", "shareNodeToPersonButton", this.shareNodeToPerson,
                this);
            var backButton = this.makeCloseButton("Close", "cancelShareNodeToPersonButton");
            var buttonBar = render.centeredButtonBar(shareButton + backButton);

            return header + "<p>Enter the username of the person you want to share this node with:</p>" + formControls
                + buttonBar;
        }

        shareNodeToPerson = (): void => {
            var targetUser = this.getInputVal("shareToUserName");
            if (!targetUser) {
                (new MessageDlg("Please enter a username")).open();
                return;
            }

            /*
             * Trigger going to server at next main page refresh
             */
            meta64.treeDirty = true;
            var thiz = this;
            util.json<json.AddPrivilegeRequest, json.AddPrivilegeResponse>("addPrivilege", {
                "nodeId": share.sharingNode.id,
                "principal": targetUser,
                "privileges": ["read", "write", "addChildren", "nodeTypeManagement"],
                "publicAppend" : false
            }, thiz.reloadFromShareWithPerson, thiz);
        }

        reloadFromShareWithPerson = (res: json.AddPrivilegeResponse): void => {
            if (util.checkSuccess("Share Node with Person", res)) {
                (new SharingDlg()).open();
            }
        }
    }
}
