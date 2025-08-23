# Amazon SES Email Configuration Guide

This guide explains how to configure and use Amazon Simple Email Service (SES) for sending emails in the Informasyx application.

## Prerequisites

1. AWS Account with SES access
2. Verified email addresses or domains in SES
3. SES moved out of sandbox mode (for production use)
4. AWS IAM user with SES permissions

## Configuration Steps

### 1. AWS SES Setup

1. **Go to AWS SES Console**
   - Navigate to the Amazon SES console in your AWS account
   - Choose your preferred region (e.g., us-east-1)

2. **Verify Email Addresses/Domains**
   ```
   - Go to "Verified identities"
   - Click "Create identity" 
   - Choose "Email address" or "Domain"
   - Enter your from email address (e.g., noreply@yourdomain.com)
   - Complete verification process
   ```

3. **Create IAM User for SES**
   ```json
   {
       "Version": "2012-10-17",
       "Statement": [
           {
               "Effect": "Allow",
               "Action": [
                   "ses:SendEmail",
                   "ses:SendRawEmail"
               ],
               "Resource": "*"
           }
       ]
   }
   ```

4. **Get Access Keys**
   - Create access key and secret key for the IAM user
   - Store these securely

### 2. Application Configuration

#### Environment Variables
Set the following environment variables:

```bash
# Required for SES
AWS_SES_ACCESS_KEY=your_access_key_here
AWS_SES_SECRET_KEY=your_secret_key_here
AWS_SES_REGION=us-east-1

# Application settings
APP_BASE_URL=https://yourdomain.com
APP_FROM_EMAIL=noreply@yourdomain.com
```

#### Profile Configuration
Use the SES profile by setting:

```bash
SPRING_PROFILES_ACTIVE=ses
```

Or in application.properties:
```properties
spring.profiles.active=ses
```

### 3. Running with SES

#### Development/Testing
```bash
# Set environment variables
export AWS_SES_ACCESS_KEY=your_access_key
export AWS_SES_SECRET_KEY=your_secret_key
export AWS_SES_REGION=us-east-1
export APP_FROM_EMAIL=noreply@yourdomain.com

# Run with SES profile
mvn spring-boot:run -Dspring-boot.run.profiles=ses
```

#### Production Deployment
```bash
# Docker example
docker run -e SPRING_PROFILES_ACTIVE=ses \
           -e AWS_SES_ACCESS_KEY=your_key \
           -e AWS_SES_SECRET_KEY=your_secret \
           -e AWS_SES_REGION=us-east-1 \
           -e APP_FROM_EMAIL=noreply@yourdomain.com \
           your-app:latest
```

### 4. Switching Between Email Providers

The application supports three email providers:

1. **Mock (Default)** - Logs emails to console
   ```properties
   app.email.provider=mock
   ```

2. **SMTP** - Traditional SMTP server
   ```properties
   app.email.provider=smtp
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   # ... other SMTP settings
   ```

3. **Amazon SES** - AWS SES service
   ```properties
   app.email.provider=ses
   aws.ses.access-key=your_key
   aws.ses.secret-key=your_secret
   aws.ses.region=us-east-1
   ```

## SES Limitations

### Sandbox Mode
- Can only send to verified email addresses
- Limited to 200 emails per day
- Maximum send rate of 1 email per second

### Production Mode (out of sandbox)
- Can send to any email address
- Higher sending limits (request increases as needed)
- Higher send rates

### Regional Availability
- SES is available in specific AWS regions
- Choose the region closest to your users for better performance
- Supported regions: us-east-1, us-west-2, eu-west-1, etc.

## Monitoring and Troubleshooting

### CloudWatch Metrics
Monitor SES usage through AWS CloudWatch:
- Send statistics
- Bounce and complaint rates
- Reputation metrics

### Logs
Check application logs for SES-related messages:
```
INFO  - Email verification sent via SES to: user@example.com
ERROR - Failed to send email via SES to: user@example.com
```

### Common Issues
1. **Authentication errors**: Check access key and secret
2. **Unverified email**: Verify sender email in SES console
3. **Rate limits**: Monitor sending rate and daily limits
4. **Regional issues**: Ensure correct region configuration

## Cost Optimization

- SES pricing: $0.10 per 1,000 emails
- No upfront costs or minimum fees
- Monitor usage through AWS Billing dashboard
- Set up CloudWatch alarms for cost control

## Security Best Practices

1. Use IAM roles instead of access keys in production
2. Implement least-privilege permissions
3. Rotate access keys regularly
4. Use AWS Secrets Manager for credential storage
5. Enable AWS CloudTrail for audit logging