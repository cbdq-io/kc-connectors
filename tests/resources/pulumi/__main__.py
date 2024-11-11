"""An Azure RM Python Pulumi program"""
import pulumi_azure_native as azure_native

resource_name = 'sbox-dev-uks'

# Create an Azure Resource Group
resource_group = azure_native.resources.ResourceGroup(
    'rg',
    resource_group_name=f'{resource_name}-rg-01',
    location='UKSouth'
)

namespace = azure_native.servicebus.Namespace(
    'sbus',
    location=resource_group.location,
    namespace_name=f'{resource_name}-sbus-01',
    resource_group_name=resource_group.name,
    sku={
        'name': azure_native.servicebus.SkuName.STANDARD,
        'tier': azure_native.servicebus.SkuTier.STANDARD
    }
)

topic_names = [
    'vault.api.v1.accounts.account.created',
    'vault.api.v1.audit_logs.audit_log.created'
]

topics = {}

for topic_name in topic_names:
    topics[topic_name] = azure_native.servicebus.Topic(
        topic_name,
        namespace_name=namespace.name,
        resource_group_name=resource_group.name,
        topic_name=topic_name
    )

subscription = azure_native.servicebus.Subscription(
    'foo-audit-log-created',
    namespace_name=namespace.name,
    resource_group_name=resource_group.name,
    subscription_name='foo-audit-log-created',
    topic_name='vault.api.v1.audit_logs.audit_log.created'
)
