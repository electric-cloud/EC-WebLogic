jms_server_name = 'Server1'
target_name = 'AdminServer'

edit()
startEdit()
cd('/')
print "Creating JMS Server %s" % jms_server_name
cmo.createJMSServer(jms_server_name)
cd("/JMSServers/%s" % jms_server_name)
cmo.addTarget(getMBean("/Servers/%s" % target_name))
activate()



