from django.db import models
from django.utils.timezone import utc

class Person(models.Model):
    FREQUENCY_CODE = (
        ('H', 'high'),
        ('M', 'medium'),
        ('L', 'low'),
    )
    name = models.CharField(max_length=100)
    fb_user_id = models.CharField(max_length=100, unique=True)
    loc_frequency = models.CharField(max_length=1, choices=FREQUENCY_CODE, default='H')
    loc_range = models.CharField(max_length=1, choices=FREQUENCY_CODE, default='H')
    created_at = models.DateTimeField(editable=False)
    updated_at = models.DateTimeField(editable=False)
    def __unicode__(self):
        return str(self.id)
    
    def save(self, *args, **kwargs):
        from datetime import datetime
        if not self.id:
            self.created_at = datetime.utcnow().replace(tzinfo=utc)
        self.updated_at = datetime.utcnow().replace(tzinfo=utc)
        super(Person, self).save(*args, **kwargs)
    

class Group(models.Model):
    name = models.CharField(max_length=150)
    created_by = models.IntegerField()
    channel_name = models.CharField(max_length=150, unique=True)
    map_data = models.CharField(max_length=15000, blank=True, null=True)
    members = models.ManyToManyField(Person, through='Membership')
    created_at = models.DateTimeField(editable=False)
    updated_at = models.DateTimeField(editable=False)
    def __unicode__(self):
        return str(self.id)
    
    def save(self, *args, **kwargs):
        from datetime import datetime
        if not self.id:
            self.created_at = datetime.utcnow().replace(tzinfo=utc)
        self.updated_at = datetime.utcnow().replace(tzinfo=utc)
        super(Group, self).save(*args, **kwargs)


class Membership(models.Model):
    class Meta:
        unique_together = ['group', 'member']

    STATUS_CODE = (
        ('I', 'invited'),
        ('A', 'accepted'),
        ('R', 'rejected'),
        ('D', 'deleted'),
    )
    group = models.ForeignKey(Group)
    member = models.ForeignKey(Person)
    invited_by = models.IntegerField()
    status = models.CharField(max_length=1, choices=STATUS_CODE, default='I')
    message = models.CharField(max_length=300, blank=True, null=True)
    created_at = models.DateTimeField(editable=False)
    updated_at = models.DateTimeField(editable=False)
    def __unicode__(self):
        return str(self.id)
    
    def save(self, *args, **kwargs):
        from datetime import datetime
        if not self.id:
            self.created_at = datetime.utcnow().replace(tzinfo=utc)
        self.updated_at = datetime.utcnow().replace(tzinfo=utc)
        super(Membership, self).save(*args, **kwargs)

