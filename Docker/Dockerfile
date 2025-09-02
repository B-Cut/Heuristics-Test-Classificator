# ========================================================================================================== #
#                                                                                                            #
# WARNING, ANYONE THAT HAS ACCESS TO THIS IMAGE COULD GET ACCESS TO THE SSH KEY PASSED TO IT, BE CAREFUL!    #
#                                                                                                            #
# unfortunately this is the quickest way to do this operation                                                #
#                                                                                                            #
# ========================================================================================================== #


FROM ubuntu

ARG git_private_ssh_key=""
ARG git_public_ssh_key=""
ARG target_repo=""
# Add maven and some common dependencies
RUN apt-get update
RUN apt-get install -y jq libtool git openssh-client openjdk-21-jre openjdk-21-jdk maven bash make nodejs libaio-dev lksctp-tools libtool automake autoconf

# Setup git
RUN mkdir -p -m 0700 ~/.ssh && ssh-keyscan github.com >> ~/.ssh/known_hosts
RUN echo "${git_private_ssh_key}" > ~/.ssh/id_ed25519 && chmod 700 ~/.ssh/id_ed25519 
RUN echo "${git_public_ssh_key}}" > ~/.ssh/id_ed25519.pub && chmod 700 ~/.ssh/id_ed25519.pub

# Clone repo
RUN git clone git@github.com:"${target_repo}".git target
WORKDIR /target
RUN ls
RUN mvn -N wrapper:wrapper
RUN ./mvnw dependency:resolve-plugins dependency:go-offline -N -B
RUN ./mvnw -DskipTests clean install


