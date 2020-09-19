import json 
import sys

#python3 generateKubernetesHosts.py ./MockFog2/node-manager/run/machines/machine_meta.jsonc kubespray/inventory/mycluster/hosts.yaml

def printAll(data):
    result = "   hosts:\n"
    for x in data:
        result += "    " + x + ":\n"

    return result


if __name__ == "__main__":
    # execute only if run as a script
    src = sys.argv[1]
    dst = sys.argv[2]

    output = open(dst, "w")
    reader = open(src, "r")
    #print(reader.read())
    data = json.loads(reader.read())

    output.writelines("all:\n")
    output.writelines(" hosts:\n")

    nodes = []

    for instance in data["instances"]:
        name = instance["tags"]["Name"]
        nodes.append(name)
        print(name)
        private_ip = ""
        
        for network_interface in instance["network_interfaces"]:
            ip = network_interface["private_ip_address"]
            if (ip.startswith("10.0.2.")):
                private_ip = ip
                break

        public_ip = instance["public_ip_address"]

        output.write("  " + name + ":\n")
        output.write("   ansible_host: " + public_ip + "\n")
        output.write("   access_ip: " + public_ip + "\n")
        output.write("   ip: " + private_ip + "\n")
        output.write("   ansible_become: yes\n")
        output.write("   ansible_connection: ssh\n")
        output.write("   ansible_user: ec2-user\n")

    output.writelines(" children:\n")
    all = printAll(sorted(nodes))
    # allOdd = nodes
    # if(len(nodes) % 2 == 0):
    #     allOdd = sorted(nodes)
    #     allOdd.pop(0)

    # allOdd = printAll(allOdd)

    last3 = printAll(sorted(nodes)[-3:])

    output.writelines("  kube-master:\n")
    output.writelines(last3)

    output.writelines("  kube-node:\n")
    output.writelines(all)
    
    output.writelines("  etcd:\n")
    output.writelines(last3)

    output.writelines("  k8s-cluster:\n")
    output.writelines("   children:\n")
    output.writelines("    kube-master:\n")
    output.writelines("    kube-node:\n")

    output.writelines("  calico-rr:\n")
    output.writelines("   hosts: {}\n")

    output.close()
    reader.close()