import urllib2, urllib
import sys, json

class Error:
    InvalidFbToken = 101
    InvalidSession = 102
    GroupInvalid = 103
    MembershipNotCreated = 104
    InvalidFriendsList = 105
    ErrorAddingFriends = 106
    ErrorUpdatingStatus = 107
    MembershipInvalid = 108
    InvalidRequestTokens = 109 

    @staticmethod
    def format_json(err_msg, err_type, err_code):
        res = {}
        error = {}
        error["message"] = err_msg
        error["type"] = err_type
        error["code"] = err_code
        res["error"] = error
        return json.dumps(res)
    
    @staticmethod
    def msg_by_code(err_code):
        if err_code == Error.InvalidFbToken:
            err_msg = "facebook token and userid dont match"
            err_type = "InvalidFbToken"
        elif err_code == Error.InvalidSession:
            err_msg = "Invalid session. Login required"
            err_type = "InvalidSession"
        elif err_code == Error.GroupInvalid:
            err_msg = "Group does not exists"
            err_type = "GroupInvalid"
        elif err_code == Error.MembershipNotCreated:
            err_msg = "Error in creating Membership"
            err_type = "MembershipNotCreated"
        elif err_code == Error.MembershipNotCreated:
            err_msg = "Error in creating Membership"
            err_type = "MembershipNotCreated"
        elif err_code == Error.InvalidFriendsList:
            err_msg = "Invalid friend list is passed"
            err_type = "InvalidFriendsList"
        elif err_code == Error.ErrorAddingFriends:
            err_msg = "Error in inviting friends to the group"
            err_type = "ErrorAddingFriends"
        elif err_code == Error.MembershipInvalid:
            err_msg = "group and person do not match"
            err_type = "MembershipInvalid"
        elif err_code == Error.InvalidRequestTokens:
            err_msg = "parameter for the request are invalid"
            err_type = "InvalidRequestTokens"
        else:
            err_msg = "Unrecognized Error"
            err_type = "Error"
        return Error.format_json(err_msg, err_type, err_code)


class Facebook:
    fb_graph_url = 'https://graph.facebook.com/'
    
    @staticmethod
    def get_profile(fb_access_token, fb_id):
        args = {'access_token':fb_access_token}
        url = Facebook.fb_graph_url+fb_id+'?'+urllib.urlencode(args)
        try:
            response = urllib2.urlopen(url)
            return response.next()
        except urllib2.HTTPError, error:
            return error.next() 
        except:
            return Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys')


    @staticmethod
    def post_feed(fb_access_token, fb_id, msg):
        args = {'access_token':fb_access_token, 'message':msg}
        url = Facebook.fb_graph_url+fb_id+'/feed'
        try:
            data = urllib.urlencode(args)
            req = urllib2.Request(url, data)
            response = urllib2.urlopen(req)
            return response.next()
        except urllib2.HTTPError, error:
            return error.next() 
        except:
            return Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys')
