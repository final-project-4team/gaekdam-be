# ==============================
# gaekdam backend deploy Makefile
# ==============================

IMAGE_NAME=gaekdam-backend
AWS_ACCOUNT=248189914600
REGION=ap-northeast-2
ECR_REPO=$(AWS_ACCOUNT).dkr.ecr.$(REGION).amazonaws.com/$(IMAGE_NAME)

# git 커밋 해시 (이미지 버전용)
TAG=$(shell git rev-parse --short HEAD)

.PHONY: deploy

deploy:
	docker buildx build --platform linux/amd64 -t $(IMAGE_NAME):$(TAG) .
	docker tag $(IMAGE_NAME):$(TAG) $(ECR_REPO):$(TAG)
	docker tag $(IMAGE_NAME):$(TAG) $(ECR_REPO):latest
	docker push $(ECR_REPO):$(TAG)
	docker push $(ECR_REPO):latest
