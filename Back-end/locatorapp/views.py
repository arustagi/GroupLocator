import json, sys
from django.shortcuts import render_to_response
from django.http import Http404, HttpResponse, HttpResponseRedirect
from helper import Error
from locatorapp.models import *
from django.views.decorators.csrf import csrf_exempt                                          


def is_fb_user_registered(f_user_id):
    if Person.objects.filter(fb_user_id=f_user_id).exists():
        person = Person.objects.get(fb_user_id=f_user_id)
        return person.id
    else:
        return 0

def create_user(f_user_id, name):
    person_id = is_fb_user_registered(f_user_id)
    if( person_id == 0):
        p = Person(fb_user_id=f_user_id, name=name)
        p.save()
        return p.id
    else:
        return person_id

def destroy_session(request):
    try:
        del request.session['member_id']
    except KeyError:
        pass
    request.session.clear()

@csrf_exempt 
def logout(request):
    destroy_session(request)
    res = {}
    res["success"] = "yes"
    return HttpResponse(json.dumps(res))

@csrf_exempt 
def login(request):
    try:
        g = request.POST
        f_access_token = g.get("fb_access_token", None)
        if ((not f_access_token)):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        from helper import Facebook
        f_profile = json.loads(Facebook.get_profile(f_access_token,'me'))
        if( 'error' in f_profile):
            destroy_session(request)
            return HttpResponse(Error.msg_by_code(Error.InvalidFbToken))
        
        request.session['member_id'] = create_user(f_profile['id'], f_profile["first_name"] + ' ' + f_profile["last_name"])
        request.session['fb_user_id'] = f_profile['id']
        request.session['fb_access_token'] = f_access_token
        request.session['first_name'] = f_profile["first_name"]
        request.session['last_name'] = f_profile["last_name"]
        res = {}
        res["success"] = "yes"
        res["member_id"] = request.session['member_id']
        return HttpResponse(json.dumps(res))
    except:
        destroy_session(request)
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))


def is_session_invalid(request):
    if 'member_id' in request.session:
        return False
    else:
        return True

@csrf_exempt 
def settings(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    try:
        p = Person.objects.get(pk=request.session["member_id"])
        res = {}
        res["loc_frequency"] = p.loc_frequency
        res["loc_range"] = p.loc_range
        return HttpResponse(json.dumps(res))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))


@csrf_exempt 
def update_settings(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    try:
        g = request.POST
        loc_freq = g.get("loc_freq", None)
        loc_range = g.get("loc_range", None)

        if ((not loc_freq) or (not loc_range)):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        p = Person.objects.get(pk=request.session["member_id"])
        res = {}
        p.loc_frequency = loc_freq 
        p.loc_range = loc_range
        p.save()
        res = {}
        res["success"] = 'yes'
        return HttpResponse(json.dumps(res))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))



def create_membership(g_id, m_to_id, m_from_id, msg, status):
    try:
        g = Group.objects.get(pk=g_id)
        m = Person.objects.get(pk=m_to_id)
        membership = Membership(group=g, member=m, invited_by=m_from_id, message=msg, status=status)
        membership.save()

        return membership.id
    except:
        return 0

@csrf_exempt 
def create_group(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    try:
        g = request.POST
        g_name = g.get("g_name", '')
        if (g_name.strip() == ''):
            g_name = None
        g_map = g.get("g_map", '')
        g_add = g.get("g_add", '')
        
        if (not g_name):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        import hashlib
        from datetime import datetime
        member_id = request.session["member_id"]
        g_channel_name = hashlib.sha1( str(datetime.time(datetime.now())) +str(member_id) + str(g_name) ).hexdigest()
        g = Group(name=g_name, channel_name=g_channel_name, map_data=g_map, created_by=member_id)
        g.save()
        
        create_membership(g.id, member_id, member_id, 'subscribing my group', 'A')
        
        if (g_add.strip() == ''): 
            fb_access_token = request.session['fb_access_token']
            res = addingFriends(g_id, g_msg, g_add, fb_access_token)
        else:
            res = {}
            res["group_id"] = g.id
        return HttpResponse(json.dumps(res))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))


@csrf_exempt 
def get_group_by_id(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    
    try:
        g = request.POST
        g_id = g.get('g_id', None)
        
        if (not g_id):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        group = Group.objects.get(pk=g_id)
        person = Person.objects.get(pk=request.session['member_id'])
        if Membership.objects.filter(group=group, member=person).exists():
            res = {}
            res["group_id"] = g_id
            res["name"] = group.name
            res["channel_name"] = group.channel_name
            res["map_data"] = group.map_data
            return HttpResponse(json.dumps(res))
        else:
            return HttpResponse(Error.msg_by_code(Error.MembershipInvalid))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))


@csrf_exempt 
def get_group_members(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    
    try:
        g = request.POST
        g_id = g.get('g_id', None)
        g_status = g.get('g_status', None)
         
        if ((not g_id) or (not g_status)):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        group = Group.objects.get(pk=g_id)
        person = Person.objects.get(pk=request.session['member_id'])
        if Membership.objects.filter(group=group, member=person).exists():
            res = {}
            res["group_id"] = g_id
            ms = []
            for membership in Membership.objects.filter(group=group, status=g_status):
                m = {}
                m['id'] = membership.member.id
                m['name'] = membership.member.name
                ms.append(m)
            res["members"] = ms
            return HttpResponse(json.dumps(res))
        else:
            return HttpResponse(Error.msg_by_code(Error.MembershipInvalid))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))



@csrf_exempt 
def get_member_groups(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    try:
        g = request.POST
        g_status = g.get('g_status', None)
        if (not g_status):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        groups = []
        person = Person.objects.get(pk=request.session['member_id'])
        for membership in Membership.objects.filter(member=person, status=g_status):
            g = {}
            g['id'] = membership.group.id
            g['name'] = membership.group.name
            groups.append(g)
        res = {}
        res["groups"] = groups
        return HttpResponse(json.dumps(res))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))



@csrf_exempt 
def update_membership_status(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    
    try:
        g = request.POST
        g_id = g.get("g_id", None)
        g_status = g.get("g_status", None)
        
        if ((not g_id) or (not g_status)):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        group = Group.objects.get(pk=g_id)
        person = Person.objects.get(pk=request.session['member_id'])
        if Membership.objects.filter(group=group, member=person).exists():
            mem = Membership.objects.get(group=group, member=person)
            mem.status = g_status
            mem.save()
            res = {}
            res["success"] = 'yes'
            return HttpResponse(json.dumps(res))
        else:
            return HttpResponse(Error.msg_by_code(Error.MembershipInvalid))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))



@csrf_exempt 
def update_group_map(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    
    try:
        g = request.POST
        g_id = g.get("g_id", None)
        g_map = g.get("g_map", None)
        
        if ((not g_id) or (not g_map)):
            return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
        group = Group.objects.get(pk=g_id)
        person = Person.objects.get(pk=request.session['member_id'])
        if Membership.objects.filter(group=group, member=person).exists():
            group.map_data = g_map
            group.save()
            res = {}
            res["success"] = 'yes'
            return HttpResponse(json.dumps(res))
        else:
            return HttpResponse(Error.msg_by_code(Error.MembershipInvalid))
    except:
        return HttpResponse(Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys'))



@csrf_exempt 
def add_subscribers_to_group(request):
    if (is_session_invalid(request)):
        return HttpResponse(Error.msg_by_code(Error.InvalidSession))
    g = request.POST
    g_id = g.get("g_id", None)
    g_add = g.get("g_add", None)
    g_msg = g.get("g_msg", '')
    
    if ((not g_id) or (not g_add)):
        return HttpResponse(Error.msg_by_code(Error.InvalidRequestTokens))
    
    fb_access_token = request.session['fb_access_token']

    res = addingFriends(g_id, g_msg, g_add, fb_access_token)
    return HttpResponse(res)


def addingFriends(g_id, g_msg, g_add, fb_access_token):
    try:
        from helper import Facebook
        f_profile = json.loads(Facebook.get_profile(fb_access_token,'me'))
        if( 'error' in f_profile):
            return Error.msg_by_code(Error.InvalidFbToken)
        
        if Group.objects.filter(pk=g_id).exists():
            g = Group.objects.get(pk=g_id)
            members = g_add.split('*')
            friends_not_added = []
            for m_id in members:
                if (m_id.strip() == ''):
                    continue
                print m_id
                f_profile = json.loads(Facebook.get_profile(request.session['fb_access_token'], m_id))
                if( 'error' in f_profile):
                    friends_not_added.append(m_id)
                #if (is_fb_user_registered(m_id) == 0):
                #    Facebook.post_feed(request.session['fb_access_token'], m_id, g_msg)
                p_id = create_user(m_id, f_profile["first_name"] + ' ' + f_profile["last_name"])
                create_membership(g_id, p_id, request.session['member_id'], g_msg, 'I')
            res = {}
            res["success"] = 'yes'
            res["group_id"] = str(g_id)
            res["friends_not_added"] = friends_not_added
            return json.dumps(res)
        else:
            return Error.msg_by_code(Error.ErrorAddingFriends)
    except:
        return Error.format_json(str(sys.exc_value), str(sys.exc_type), 'sys')

