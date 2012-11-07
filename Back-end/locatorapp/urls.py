from django.conf.urls.defaults import *

import views

urlpatterns = patterns('', 
    url(r'^login/', views.login, name='login'),
    url(r'^logout/', views.logout, name='logout'),
    url(r'^settings/', views.settings, name='settings'),
    url(r'^update_settings/', views.update_settings, name='update_settings'),
    url(r'^create_group/', views.create_group, name='create_group'),
    url(r'^get_group_by_id/', views.get_group_by_id, name='get_group_by_id'),
    url(r'^get_group_members/', views.get_group_members, name='get_group_members'),
    url(r'^get_member_groups/', views.get_member_groups, name='get_member_groups'),
    url(r'^update_membership_status/', views.update_membership_status, name='update_membership_status'),
    url(r'^update_group_map/', views.update_group_map, name='update_group_map'),
    url(r'^add_subscribers_to_group/', views.add_subscribers_to_group, name='add_subscribers_to_group'),
    )
