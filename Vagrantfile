# Each stage is one virtual machine (VM).
# Each stage uses its own box (operating system image).
STAGES = {
  "dev"  => { box: "bento/ubuntu-24.04", ip: "192.168.56.11" },
  "test" => { box: "bento/ubuntu-24.04", ip: "192.168.56.12" },
  "uat"  => { box: "bento/ubuntu-24.04", ip: "192.168.56.13" },
}

Vagrant.configure("2") do |config|
  STAGES.each do |name, opts|
    config.vm.define name do |node|
      node.vm.box      = opts[:box]
      node.vm.hostname = "myapp-#{name}"
      node.vm.network "private_network", ip: opts[:ip]
      node.vm.provider "virtualbox" do |vb|
        vb.memory = 1024
      end

      # Install Java 25 runtime so the box can run app.jar
      node.vm.provision "shell", inline: <<~SCRIPT
        apt-get update
        apt-get install -y openjdk-25-jre-headless
      SCRIPT
    end
  end
end
