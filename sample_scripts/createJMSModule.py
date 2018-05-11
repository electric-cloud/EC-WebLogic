resource_name = 'test'
target = 'AdminServer'

cd('/')
if cmo.lookupJMSSystemResource(resource_name):
    print "Resource %s alreay exists" % resource_name
else:
    startEdit()
    cmo.createJMSSystemResource(resource_name)
    cd("/JMSSystemResources/%s" % resource_name)
    cmo.addTarget(getMBean("/Servers/%s" % target))
    # cmo.createSubDeployment(’subdeployment0′)
    activate()

